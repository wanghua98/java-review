package com.uniplore.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.uniplore.config.LogRecord;
import com.uniplore.pojo.*;
import com.uniplore.result.Result;
import com.uniplore.result.ResultMessage;
import com.uniplore.service.FileChunkService;
import com.uniplore.service.FileDirectoryService;
import com.uniplore.service.FileUploadTaskService;
import com.uniplore.mapper.FileInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 文件相关接口控制器
 *
 * @author dao
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    /**
     * 文件分块服务
     */
    private final FileChunkService fileChunkService;

    /**
     * 文件任务
     */
    private final FileUploadTaskService fileUploadTaskService;

    /**
     * 目录服务
     */
    private final FileDirectoryService fileDirectoryService;

    /**
     * 上传文件物理存储路径
     */
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 文件信息Mapper
     */
    private final FileInfoMapper fileInfoMapper;


    /**
     * 文件初始化接口
     * <p>
     * 前端上传文件前先调用此接口初始化上传任务，返回任务ID等信息用于后续分片上传。
     * 若文件已存在（SHA-256匹配）则直接秒传，无需实际上传。
     * </p>
     *
     * @param fileUploadTask 文件上传任务信息（含fileName、fileSha256、fileSize、chunkCount等）
     * @return 上传任务信息（含任务ID）
     */
    @LogRecord("初始化上传任务")
    @PostMapping("/init")
    public Result<FileUploadTask> initFile(@RequestBody FileUploadTask fileUploadTask) {
        // 校验必要参数（SHA-256 可为空，大文件由后端在合并时计算）
        if (fileUploadTask.getFileName() == null) {
            return Result.error(400, "参数有误", null);
        }
        // 判断是否登陆
        if (!StpUtil.isLogin()) {
            return Result.error(400, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }

        return fileUploadTaskService.initFile(fileUploadTask);
    }


    /**
     * 上传分片接口
     * <p>
     * 接收单个分片文件，保存到本地临时目录。
     * 若该分片已通过 Redis 判重则直接跳过。
     * 当所有分片上传完成后自动触发合并操作。
     * </p>
     *
     * @param taskId      上传任务ID
     * @param chunkNumber 分片编号（从1开始）
     * @param file        分片文件内容
     * @return 上传结果
     */
    @LogRecord("上传分片")
    @PostMapping("/upload")
    public Result<String> uploadChunk(@RequestParam("taskId") String taskId,
                                      @RequestParam("chunkNumber") Integer chunkNumber,
                                      @RequestParam("file") MultipartFile file) throws IOException, NoSuchAlgorithmException {

        // 参数校验
        if (file == null || taskId == null || chunkNumber == null) {
            return Result.error(400, "参数有误", null);
        }

        // 将taskId转为Long，防止数值溢出
        long parsedTaskId;
        try {
            parsedTaskId = Long.parseLong(taskId);
        } catch (NumberFormatException e) {
            return Result.error(400, "taskId格式错误: " + taskId, null);
        }

        // 创建分片文件信息对象
        FileChunk fileChunk = new FileChunk();
        // 设置分片文件信息
        fileChunk.setTaskId(parsedTaskId);
        fileChunk.setChunkNumber(chunkNumber);
        // 保存分片文件
        return fileChunkService.saveChunk(fileChunk, file);
    }


    /**
     * 获取当前用户目录下的文件夹以及文件（分页）
     * <p>
     * 查询当前登录用户的个人目录，返回其下的子目录列表（按sort排序）和文件列表（按上传时间倒序，分页）。
     * 用于前端登录后展示用户的文件管理首页。
     * </p>
     *
     * @param page 当前页码（从1开始，默认1）
     * @param size 每页条数（默认20）
     * @return 目录文件列表
     */
    @LogRecord("获取根目录列表")
    @GetMapping("/dir/list")
    public Result<DirectoryVO> getUserDirList(@RequestParam(value = "page", defaultValue = "1") int page,
                                               @RequestParam(value = "size", defaultValue = "20") int size) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        // 查询当前用户目录下内容
        DirectoryVO vo = fileDirectoryService.getUserDirectoryContents(StpUtil.getLoginIdAsLong(), page, size);
        if (vo == null) {
            return Result.error(400, "用户目录不存在", null);
        }
        return Result.success(vo);
    }


    /**
     * 查看指定目录下的文件以及目录（分页）
     * <p>
     * 根据目录ID查询其下的子目录和分页后的文件列表。
     * 前端点击目录树或面包屑导航时调用此接口进入子目录。
     * </p>
     *
     * @param dirId 目录ID
     * @param page  当前页码（从1开始，默认1）
     * @param size  每页条数（默认20）
     * @return 目录文件列表
     */
    @LogRecord("获取目录内容")
    @GetMapping("/dir/list/{dirId}")
    public Result<DirectoryVO> getDirList(@PathVariable Long dirId,
                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "size", defaultValue = "20") int size) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        // 参数校验
        if (dirId == null || dirId <= 0) {
            return Result.error(400, ResultMessage.INVALID_PARAMETERS.getMessage(), null);
        }
        // 查询指定目录下内容
        DirectoryVO vo = fileDirectoryService.getDirectoryContents(dirId, page, size);
        if (vo == null) {
            return Result.error(400, "目录不存在", null);
        }
        return Result.success(vo);
    }


    /**
     * 下载文件
     * <p>
     * 根据文件ID查询文件信息，从存储路径读取文件并返回下载流。
     * 同时记录文件下载操作日志。
     * </p>
     *
     * @param fileId 文件ID
     * @return 文件下载响应
     */
    @LogRecord("下载文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {


        // 查询文件信息
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null || fileInfo.getStatus() == null || fileInfo.getStatus() < 1) {
            return ResponseEntity.badRequest().build();
        }

        // 拼接完整存储路径
        String storagePath = fileInfo.getStoragePath();
        String fullPath = uploadPath + File.separator + storagePath;
        File file = new File(fullPath);

        // 检查文件是否存在
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.badRequest().build();
        }

        // 创建文件资源
        FileSystemResource resource = new FileSystemResource(file);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        // 文件名URL编码，支持中文文件名
        String encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedFileName);
        // 根据文件后缀设置 Content-Type
        MediaType mediaType = getMediaType(fileInfo.getFileSuffix());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * 预览文件接口
     * @param fileId 文件对应id
     * @param suffix 文件对应后缀
     * @return 对应文件
     */
    @LogRecord("预览文件")
    @GetMapping("/inline/{fileId}.{suffix}")
    public ResponseEntity<Resource> inlineFile(@PathVariable Long fileId, @PathVariable(required = false) String suffix) {

        // 查询文件信息
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null || fileInfo.getStatus() == null || fileInfo.getStatus() < 1 || suffix == null || suffix.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 拼接完整存储路径
        String storagePath = fileInfo.getStoragePath();
        String fullPath = uploadPath + File.separator + storagePath;
        File file = new File(fullPath);

        // 检查文件是否存在
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.badRequest().build();
        }

        // 创建文件资源
        FileSystemResource resource = new FileSystemResource(file);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        // 文件名URL编码，支持中文文件名
        String encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename*=UTF-8''" + encodedFileName);
        // 根据文件后缀设置 Content-Type
        MediaType mediaType = getMediaType(fileInfo.getFileSuffix());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * 在指定目录下新建子目录
     * <p>
     * 在父目录下创建一个新的子目录，名称不能和同级目录重复。
     * </p>
     *
     * @param parentId 父目录ID
     * @param name     目录名称
     * @return 创建结果
     */
    @LogRecord("创建目录")
    @PostMapping("/dir/create")
    public Result<FileDirectory> createDir(@RequestParam("parentId") Long parentId,
                                           @RequestParam("name") String name) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return Result.error(400, ResultMessage.INVALID_PARAMETERS.getMessage(), null);
        }
        try {
            FileDirectory dir = fileDirectoryService.createSubDirectory(
                    parentId, name.trim(), StpUtil.getLoginIdAsLong());
            return Result.success(dir);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }


    /**
     * 获取当前用户的所有目录列表
     * <p>
     * 查询当前用户创建的所有目录（平铺列表，按父目录、sort 排序）。
     * 用于前端移动文件时展示所有可选目录。
     * </p>
     *
     * @return 目录列表
     */
    @LogRecord("获取所有目录")
    @GetMapping("/dir/all")
    public Result<List<FileDirectory>> getAllDirs() {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        List<FileDirectory> dirs = fileDirectoryService.getAllUserDirectories(StpUtil.getLoginIdAsLong());
        return Result.success(dirs);
    }


    /**
     * 移动文件到其他目录
     * <p>
     * 将指定文件移动到目标目录下（只更新数据库中的 parent_id，不移动磁盘文件）。
     * </p>
     *
     * @param fileId      文件ID
     * @param targetDirId 目标目录ID
     * @return 移动结果
     */
    @LogRecord("移动文件")
    @PostMapping("/move")
    public Result<String> moveFile(@RequestParam("fileId") Long fileId,
                                   @RequestParam("targetDirId") Long targetDirId) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        // 查询文件是否存在
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            return Result.error(400, "文件不存在", null);
        }
        // 检查目标目录是否存在
        FileDirectory targetDir = fileDirectoryService.getById(targetDirId);
        if (targetDir == null) {
            return Result.error(400, "目标目录不存在", null);
        }
        // 自动重命名：如果目标目录已存在同名文件，追加编号避免冲突
        String uniqueName = fileDirectoryService.resolveUniqueFileName(
                targetDirId, fileInfo.getFileName());
        fileInfo.setFileName(uniqueName);
        // 更新文件所在目录
        fileInfo.setParentId(targetDirId);
        fileInfoMapper.updateById(fileInfo);

        return Result.success("移动成功");
    }


    /**
     * 移动目录到其他目录
     * <p>
     * 将指定目录移动到目标目录下（只更新数据库中的 parent_id）。
     * 不能移动到自身或自己的子树中。
     * </p>
     *
     * @param dirId       目录ID
     * @param targetDirId 目标目录ID
     * @return 移动结果
     */
    @LogRecord("移动目录")
    @PostMapping("/dir/move/{dirId}")
    public Result<String> moveDir(@PathVariable Long dirId,
                                   @RequestParam("targetDirId") Long targetDirId) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        try {
            fileDirectoryService.moveDirectory(dirId, targetDirId, StpUtil.getLoginIdAsLong());
            return Result.success("移动成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }


    /**
     * 删除文件
     * <p>
     * 将指定文件标记为已删除状态（软删除，status=0），
     * 物理文件保留磁盘以供其他引用（秒传复用）继续访问。
     * 只有文件的上传者才能删除。
     * </p>
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    @LogRecord("删除文件")
    @PostMapping("/delete/{fileId}")
    public Result<String> deleteFile(@PathVariable Long fileId) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        try {
            fileDirectoryService.deleteFile(fileId, StpUtil.getLoginIdAsLong());
            return Result.success("删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }


    /**
     * 删除目录
     * <p>
     * 递归删除指定目录及其下所有文件和子目录（软删除）。
     * 不允许删除根目录和用户个人目录。
     * </p>
     *
     * @param dirId 目录ID
     * @return 删除结果
     */
    @LogRecord("删除目录")
    @PostMapping("/dir/delete/{dirId}")
    public Result<String> deleteDir(@PathVariable Long dirId) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        try {
            fileDirectoryService.deleteDirectory(dirId, StpUtil.getLoginIdAsLong());
            return Result.success("删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }


    /**
     * 重命名文件
     * <p>
     * 将指定文件重命名为新名称。名称冲突时自动追加编号后缀。
     * 只有文件的上传者才能重命名。
     * </p>
     *
     * @param fileId  文件ID
     * @param newName 新文件名（含扩展名）
     * @return 重命名结果
     */
    @LogRecord("重命名文件")
    @PostMapping("/rename/{fileId}")
    public Result<String> renameFile(@PathVariable Long fileId,
                                     @RequestParam("newName") String newName) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        try {
            fileDirectoryService.renameFile(fileId, newName, StpUtil.getLoginIdAsLong());
            return Result.success("重命名成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }

    /**
     * 重命名目录
     * <p>
     * 将指定目录重命名为新名称。名称冲突时自动追加编号后缀。
     * 不允许重命名 id=2 的目录。
     * 只有目录的创建者才能重命名。
     * </p>
     *
     * @param dirId   目录ID
     * @param newName 新目录名
     * @return 重命名结果
     */
    @LogRecord("重命名目录")
    @PostMapping("/dir/rename/{dirId}")
    public Result<String> renameDir(@PathVariable Long dirId,
                                    @RequestParam("newName") String newName) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        try {
            fileDirectoryService.renameDirectory(dirId, newName, StpUtil.getLoginIdAsLong());
            return Result.success("重命名成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }

    /**
     * 根据文件后缀获取对应的 MediaType
     * <p>
     * 支持常见文件类型的 MIME 映射，默认返回 application/octet-stream。
     * </p>
     *
     * @param suffix 文件后缀（如 "jpg"、"pdf"、"zip"），不含点
     * @return MediaType 对象
     */
    private MediaType getMediaType(String suffix) {
        if (suffix == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return switch (suffix.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "webp" -> MediaType.valueOf("image/webp");
            case "svg" -> MediaType.valueOf("image/svg+xml");
            case "mp4" -> MediaType.valueOf("video/mp4");
            case "avi" -> MediaType.valueOf("video/x-msvideo");
            case "mov" -> MediaType.valueOf("video/quicktime");
            case "mp3" -> MediaType.valueOf("audio/mpeg");
            case "wav" -> MediaType.valueOf("audio/wav");
            case "pdf" -> MediaType.valueOf("application/pdf");
            case "doc", "docx" -> MediaType.valueOf("application/msword");
            case "xls", "xlsx" -> MediaType.valueOf("application/vnd.ms-excel");
            case "ppt", "pptx" -> MediaType.valueOf("application/vnd.ms-powerpoint");
            case "zip" -> MediaType.valueOf("application/zip");
            case "rar" -> MediaType.valueOf("application/vnd.rar");
            case "txt" -> MediaType.valueOf("text/plain");
            case "json" -> MediaType.valueOf("application/json");
            case "xml" -> MediaType.valueOf("application/xml");
            // 文本/代码文件（让浏览器直接展示，不弹出下载框）
            case "java", "py", "js", "ts", "css", "html", "htm", "php" ->
                    MediaType.valueOf("text/plain");
            case "md" -> MediaType.valueOf("text/markdown");
            case "yaml", "yml" -> MediaType.valueOf("application/x-yaml");
            case "sh", "bat", "cmd" -> MediaType.valueOf("text/plain");
            case "c", "cpp", "h", "hpp", "cs", "go", "rs", "swift" ->
                    MediaType.valueOf("text/plain");
            case "properties", "cfg", "conf", "ini" -> MediaType.valueOf("text/plain");
            case "log" -> MediaType.valueOf("text/plain");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
