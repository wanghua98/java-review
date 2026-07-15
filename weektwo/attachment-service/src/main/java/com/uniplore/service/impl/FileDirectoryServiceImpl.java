package com.uniplore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileDirectoryMapper;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.pojo.DirectoryVO;
import com.uniplore.pojo.FileDirectory;
import com.uniplore.pojo.FileInfo;
import com.uniplore.service.FileDirectoryService;
import com.uniplore.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 目录Service实现类
 *
 * @author yf
 */
@Service
@RequiredArgsConstructor
public class FileDirectoryServiceImpl extends ServiceImpl<FileDirectoryMapper, FileDirectory> implements FileDirectoryService {

    private final FileInfoMapper fileInfoMapper;

    /**
     * 获取根目录
     *
     * @return 根目录
     */
    @Override
    public FileDirectory getRootDirectory() {
        return getOne(new QueryWrapper<FileDirectory>().eq("parent_id", 0).last("LIMIT 1"));
    }

    /**
     * 创建用户个人目录
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 创建的用户个人目录
     * @throws IllegalStateException 如果根目录不存在
     */
    @Override
    public FileDirectory createUserDirectory(Long userId, String username) {
        FileDirectory root = getRootDirectory();
        if (root == null) {
            throw new IllegalStateException("根目录不存在，请先初始化系统");
        }

        // 用户个人目录放在根目录下的 User 子目录中
        FileDirectory userParent = getUserParentDirectory();
        if (userParent == null) {
            throw new IllegalStateException("User父目录不存在，请先初始化系统");
        }

        FileDirectory userDir = new FileDirectory();
        userDir.setParentId(userParent.getId());
        userDir.setName(username);
        userDir.setSort(0);
        userDir.setCreateUser(userId);
        userDir.setStatus(1);
        save(userDir);
        return userDir;
    }

    /**
     * 根据用户ID获取用户个人目录
     *
     * @param userId 用户ID
     * @return 用户个人目录
     * @throws IllegalStateException 如果用户个人目录不存在
     */
    @Override
    public FileDirectory getUserDirectoryByUserId(Long userId) {
        return getOne(new QueryWrapper<FileDirectory>()
                .eq("create_user", userId)
                .eq("status", 1)
                .orderByAsc("id")
                .last("LIMIT 1"));
    }

    /**
     * 重命名用户个人目录
     *
     * @param userId  用户ID
     * @param newName 新目录名
     * @return 重命名后的用户个人目录
     * @throws IllegalStateException 如果用户个人目录不存在
     */
    @Override
    public FileDirectory renameUserDirectory(Long userId, String newName) {
        // 检查用户个人目录是否存在
        FileDirectory userDir = getUserDirectoryByUserId(userId);
        if (userDir == null) {
            throw new IllegalStateException("用户个人目录不存在");
        }
        // 更新目录名
        userDir.setName(newName);
        // 更新数据库
        updateById(userDir);
        return userDir;
    }

    /**
     * 获取当前用户目录下的文件夹以及文件
     * <p>
     * 根据用户ID查询其个人目录，然后获取该目录下的所有子目录和文件。
     * </p>
     *
     * @param userId 当前用户ID
     * @return 目录文件列表，若用户目录不存在返回null
     */
    @Override
    public DirectoryVO getUserDirectoryContents(Long userId) {
        // 查询当前用户的个人目录
        FileDirectory userDir = getUserDirectoryByUserId(userId);
        if (userDir == null) {
            return null;
        }
        // 获取该目录下的子目录和文件
        return getDirectoryContents(userDir.getId());
    }

    /**
     * 查看指定目录下的子目录和文件
     * <p>
     * 根据目录ID查询其下的子目录（按 sort 正序）和文件（按 create_time 倒序）。
     * </p>
     *
     * @param directoryId 目录ID
     * @return 目录文件列表，若目录不存在返回null
     */
    @Override
    public DirectoryVO getDirectoryContents(Long directoryId) {
        // 查询当前目录信息
        FileDirectory currentDir = getById(directoryId);
        if (currentDir == null) {
            return null;
        }

        // 查询子目录列表（按照 sort 正序排列）
        List<FileDirectory> subDirs = list(
                new QueryWrapper<FileDirectory>()
                        .eq("parent_id", directoryId)
                        .eq("status", 1)
                        .orderByAsc("sort")
        );

        // 查询当前目录下的文件列表（按照上传时间倒序）
        List<FileInfo> files = fileInfoMapper.selectList(
                new QueryWrapper<FileInfo>()
                        .eq("parent_id", directoryId)
                        .eq("status", 1)
                        .orderByDesc("create_time")
        );

        return new DirectoryVO(currentDir, subDirs, files);
    }

    /**
     * 在指定目录下创建子目录
     * <p>
     * 检查父目录存在且同一级没有同名目录后，创建新的子目录。
     * </p>
     *
     * @param parentId 父目录ID
     * @param name     目录名称
     * @param userId   当前用户ID
     * @return 创建的目录
     * @throws IllegalArgumentException 父目录不存在或名称重复
     */
    @Override
    public FileDirectory createSubDirectory(Long parentId, String name, Long userId) {
        // 校验目录名称格式
        ValidateUtil.validateDirectoryName(name);

        // 检查父目录是否存在
        FileDirectory parent = getById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父目录不存在");
        }

        // 自动解析不重名的目录名（如果同级存在同名则追加编号）
        String uniqueName = resolveUniqueDirName(parentId, name.trim());

        // 创建新目录
        FileDirectory dir = new FileDirectory();
        dir.setParentId(parentId);
        dir.setName(uniqueName);
        dir.setSort(0);
        dir.setCreateUser(userId);
        dir.setStatus(1);
        save(dir);
        return dir;
    }

    /**
     * 获取当前用户的所有目录（平铺列表）
     * <p>
     * 查询当前用户创建的所有状态正常的目录，
     * 按父目录和 sort 排序，用于前端移动文件时展示完整目录树。
     * </p>
     *
     * @param userId 用户ID
     * @return 目录列表
     */
    @Override
    public List<FileDirectory> getAllUserDirectories(Long userId) {
        return list(new QueryWrapper<FileDirectory>()
                .eq("create_user", userId)
                .eq("status", 1)
                .orderByAsc("parent_id", "sort")
        );
    }

    /**
     * 删除文件
     * <p>
     * 软删除：将 file_info 的 status 置为 0，物理文件保留。
     * 只有文件的上传者才能删除。
     * </p>
     *
     * @param fileId 文件ID
     * @param userId 当前用户ID
     * @return true 删除成功
     * @throws IllegalArgumentException 文件不存在或无权操作
     */
    @Override
    public boolean deleteFile(Long fileId, Long userId) {
        // 查询文件是否存在
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        // 校验所有权
        if (!fileInfo.getCreateUser().equals(userId)) {
            throw new IllegalArgumentException("无权删除此文件");
        }
        // 软删除：status = 0
        fileInfo.setStatus(0);
        fileInfoMapper.updateById(fileInfo);
        return true;
    }

    /**
     * 删除目录
     * <p>
     * 递归软删除指定目录及其下所有文件和子目录。
     * 不允许删除根目录（parent_id = 0）和用户个人目录。
     * 整个操作在一个事务中执行，任一子操作失败则全部回滚。
     * </p>
     *
     * @param dirId  目录ID
     * @param userId 当前用户ID
     * @return true 删除成功
     * @throws IllegalArgumentException 目录不存在或无权操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDirectory(Long dirId, Long userId) {
        // 查询目录是否存在
        FileDirectory dir = getById(dirId);
        if (dir == null) {
            throw new IllegalArgumentException("目录不存在");
        }
        // 不允许删除根目录
        if (dir.getParentId() == 0) {
            throw new IllegalArgumentException("不允许删除根目录");
        }
        // 不允许删除User父目录
        FileDirectory userParent = getUserParentDirectory();
        if (userParent != null && dir.getId().equals(userParent.getId())) {
            throw new IllegalArgumentException("不允许删除User目录");
        }
        // 不允许删除用户个人目录（User目录的直接子目录）
        if (userParent != null && dir.getParentId().equals(userParent.getId())) {
            throw new IllegalArgumentException("不允许删除个人目录");
        }
        // 校验所有权
        if (!dir.getCreateUser().equals(userId)) {
            throw new IllegalArgumentException("无权删除此目录");
        }

        // 递归删除子文件
        List<FileInfo> subFiles = fileInfoMapper.selectList(
                new QueryWrapper<FileInfo>()
                        .eq("parent_id", dirId)
                        .eq("status", 1)
        );
        for (FileInfo file : subFiles) {
            deleteFile(file.getId(), userId);
        }

        // 递归删除子目录
        List<FileDirectory> subDirs = list(
                new QueryWrapper<FileDirectory>()
                        .eq("parent_id", dirId)
                        .eq("status", 1)
        );
        for (FileDirectory subDir : subDirs) {
            deleteDirectory(subDir.getId(), userId);
        }

        // 删除自身
        dir.setStatus(0);
        updateById(dir);
        return true;
    }

    /**
     * 在目标目录下为文件解析一个不重名的文件名
     * <p>
     * 如果目标目录中已存在同名文件，则自动生成带编号的新文件名，
     * 命名规则：原文件名 (1).扩展名、原文件名 (2).扩展名 ...
     * </p>
     *
     * @param parentId         目标目录ID
     * @param originalFileName 原始文件名
     * @return 解析后的唯一文件名
     */
    @Override
    public String resolveUniqueFileName(Long parentId, String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return originalFileName;
        }

        // 先检查原始名称是否冲突
        long exists = fileInfoMapper.selectCount(new QueryWrapper<FileInfo>()
                .eq("parent_id", parentId)
                .eq("file_name", originalFileName)
                .eq("status", 1)
        );
        if (exists == 0) {
            return originalFileName;
        }

        // 拆分基本名和扩展名（扩展名取最后一个 . 之后的部分）
        int lastDot = originalFileName.lastIndexOf('.');
        String base;
        String ext;
        if (lastDot > 0) {
            base = originalFileName.substring(0, lastDot);
            // 包含点号，如 ".pdf"
            ext = originalFileName.substring(lastDot);

        } else {
            base = originalFileName;
            ext = "";
        }

        // 去掉已有的 (N) 后缀，避免嵌套编号
        base = base.replaceAll("\\s*\\(\\d+\\)$", "");

        // 递增编号直到找到不冲突的文件名
        int counter = 1;
        while (true) {
            String candidate = base + " (" + counter + ")" + ext;
            long count = fileInfoMapper.selectCount(new QueryWrapper<FileInfo>()
                    .eq("parent_id", parentId)
                    .eq("file_name", candidate)
                    .eq("status", 1)
            );
            if (count == 0) {
                return candidate;
            }
            counter++;
        }
    }

    /**
     * 在目标目录下为子目录解析一个不重名的目录名
     * <p>
     * 规则与 resolveUniqueFileName 一致，但查询的是 file_directory 表。
     * </p>
     *
     * @param parentId        目标父目录ID
     * @param originalDirName 原始目录名
     * @return 解析后的唯一目录名
     */
    @Override
    public String resolveUniqueDirName(Long parentId, String originalDirName) {
        if (originalDirName == null || originalDirName.isEmpty()) {
            return originalDirName;
        }

        // 先检查原始名称是否冲突
        long exists = count(new QueryWrapper<FileDirectory>()
                .eq("parent_id", parentId)
                .eq("name", originalDirName)
                .eq("status", 1)
        );
        if (exists == 0) {
            return originalDirName;
        }

        // 去掉已有的 (N) 后缀，避免嵌套编号
        String base = originalDirName.replaceAll("\\s*\\(\\d+\\)$", "");

        // 递增编号直到找到不冲突的目录名
        int counter = 1;
        while (true) {
            String candidate = base + " (" + counter + ")";
            long count = count(new QueryWrapper<FileDirectory>()
                    .eq("parent_id", parentId)
                    .eq("name", candidate)
                    .eq("status", 1)
            );
            if (count == 0) {
                return candidate;
            }
            counter++;
        }
    }

    /**
     * 重命名文件
     * <p>
     * 验证文件存在及所有权，自动解决同名冲突后更新文件名。
     * </p>
     *
     * @param fileId  文件ID
     * @param newName 新文件名
     * @param userId  当前用户ID
     * @return true 重命名成功
     * @throws IllegalArgumentException 文件不存在、无权操作或名称无效
     */
    @Override
    public boolean renameFile(Long fileId, String newName, Long userId) {
        // 查询文件是否存在
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        // 校验所有权
        if (!fileInfo.getCreateUser().equals(userId)) {
            throw new IllegalArgumentException("无权重命名此文件");
        }
        // 校验文件名格式（含非法字符和长度）
        ValidateUtil.validateFileName(newName);
        newName = newName.trim();

        // 如果名称没有变化则跳过
        if (newName.equals(fileInfo.getFileName())) {
            return true;
        }

        // 自动解决同名冲突（同一目录下不能有同名文件）
        String uniqueName = resolveUniqueFileName(fileInfo.getParentId(), newName);

        // 更新文件名
        fileInfo.setFileName(uniqueName);

        // 同步更新文件后缀（允许修改扩展名）
        int lastDot = uniqueName.lastIndexOf('.');
        String newSuffix = lastDot > 0 ? uniqueName.substring(lastDot + 1) : "";
        fileInfo.setFileSuffix(newSuffix);

        fileInfoMapper.updateById(fileInfo);
        return true;
    }

    /**
     * 重命名目录
     * <p>
     * 验证目录存在及所有权，不允许重命名 id=2 的目录，
     * 自动解决同名冲突后更新目录名。
     * </p>
     *
     * @param dirId   目录ID
     * @param newName 新目录名
     * @param userId  当前用户ID
     * @return true 重命名成功
     * @throws IllegalArgumentException 目录不存在、无权操作、受保护或名称无效
     */
    @Override
    public boolean renameDirectory(Long dirId, String newName, Long userId) {
        // 查询目录是否存在
        FileDirectory dir = getById(dirId);
        if (dir == null) {
            throw new IllegalArgumentException("目录不存在");
        }
        // id=2 的 User 父目录不能重命名
        if (dir.getId() == 2) {
            throw new IllegalArgumentException("不允许重命名此目录");
        }
        // 校验所有权
        if (!dir.getCreateUser().equals(userId)) {
            throw new IllegalArgumentException("无权重命名此目录");
        }
        // 校验目录名称格式
        ValidateUtil.validateDirectoryName(newName);
        newName = newName.trim();

        // 如果名称没有变化则跳过
        if (newName.equals(dir.getName())) {
            return true;
        }

        // 自动解决同名冲突（同一父目录下不能有同名目录）
        String uniqueName = resolveUniqueDirName(dir.getParentId(), newName);

        // 更新目录名
        dir.setName(uniqueName);
        updateById(dir);
        return true;
    }

    /**
     * 移动目录到其他目录
     * <p>
     * 验证目录存在及所有权，不允许移动自身或移动到自己的子树中，
     * 不允许移动根目录和 id=2 的目录。
     * 自动解决同名冲突后更新 parent_id。
     * </p>
     *
     * @param dirId       目录ID
     * @param targetDirId 目标父目录ID
     * @param userId      当前用户ID
     * @return true 移动成功
     * @throws IllegalArgumentException 目录不存在、无权操作、移动路径非法或目标无效
     */
    @Override
    public boolean moveDirectory(Long dirId, Long targetDirId, Long userId) {
        // 查询源目录是否存在
        FileDirectory dir = getById(dirId);
        if (dir == null) {
            throw new IllegalArgumentException("目录不存在");
        }
        // 不允许移动根目录
        if (dir.getParentId() == 0) {
            throw new IllegalArgumentException("不允许移动根目录");
        }
        // id=2 的 User 父目录不能移动
        if (dir.getId() == 2) {
            throw new IllegalArgumentException("不允许移动此目录");
        }
        // 校验所有权
        if (!dir.getCreateUser().equals(userId)) {
            throw new IllegalArgumentException("无权限移动此目录");
        }

        // 查询目标目录是否存在
        FileDirectory targetDir = getById(targetDirId);
        if (targetDir == null) {
            throw new IllegalArgumentException("目标目录不存在");
        }

        // 不能移动到自己下面
        if (dirId.equals(targetDirId)) {
            throw new IllegalArgumentException("不能将目录移动到自己内部");
        }

        // 不能移动到自己原来的位置
        if (dir.getParentId().equals(targetDirId)) {
            // 已在目标目录中，视为成功
            return true;
        }

        // 检查 targetDir 是否是 dir 的子目录（防止循环）
        if (isDescendantOf(targetDirId, dirId)) {
            throw new IllegalArgumentException("不能将目录移动到自己的子目录中");
        }

        // 自动解决同名冲突（目标目录下不能有同名目录）
        String uniqueName = resolveUniqueDirName(targetDirId, dir.getName());

        // 更新目录的父节点和名称（如果有冲突自动重命名）
        dir.setParentId(targetDirId);
        dir.setName(uniqueName);
        updateById(dir);
        return true;
    }

    /**
     * 判断 candidateId 是否是 ancestorId 的后代目录
     * <p>
     * 从 candidateId 开始沿 parentId 链向上查找，如果最终追溯到 ancestorId 则说明是后代。
     * </p>
     *
     * @param candidateId 待检查的目录ID
     * @param ancestorId  祖先目录ID
     * @return true 如果 candidateId 是 ancestorId 的后代
     */
    private boolean isDescendantOf(Long candidateId, Long ancestorId) {
        // 最多向上追溯 100 层，防止死循环（如果数据有环）
        int maxDepth = 100;
        Long currentId = candidateId;
        for (int i = 0; i < maxDepth; i++) {
            if (currentId == null || currentId == 0) {
                return false;
            }
            if (currentId.equals(ancestorId)) {
                return true;
            }
            FileDirectory current = getById(currentId);
            if (current == null) {
                return false;
            }
            currentId = current.getParentId();
        }
        return false;
    }

    /**
     * 获取User父目录（位于根目录下名为"User"的子目录）
     *
     * @return User父目录，不存在返回null
     */
    private FileDirectory getUserParentDirectory() {
        FileDirectory root = getRootDirectory();
        if (root == null) {
            return null;
        }
        return getOne(new QueryWrapper<FileDirectory>()
                .eq("parent_id", root.getId())
                .eq("name", "User")
                .eq("status", 1)
                .last("LIMIT 1"));
    }

}
