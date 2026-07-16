package com.uniplore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.DirectoryVO;
import com.uniplore.pojo.FileDirectory;

import java.util.List;

/**
 * 目录Service接口
 *
 * @author yf
 */
public interface FileDirectoryService extends IService<FileDirectory> {

    /**
     * 获取根目录
     *
     * @return 根目录对象，若不存在返回null
     */
    FileDirectory getRootDirectory();

    /**
     * 创建用户个人目录
     *
     * @param userId   用户ID
     * @param username 用户名（作为目录名）
     * @return 创建的目录
     */
    FileDirectory createUserDirectory(Long userId, String username);

    /**
     * 根据用户ID查找其个人目录
     *
     * @param userId 用户ID
     * @return 用户个人目录，若不存在返回null
     */
    FileDirectory getUserDirectoryByUserId(Long userId);

    /**
     * 重命名用户个人目录
     *
     * @param userId  用户ID
     * @param newName 新目录名称
     * @return 更新后的目录
     */
    FileDirectory renameUserDirectory(Long userId, String newName);

    /**
     * 获取当前用户目录下的文件夹以及文件（分页）
     * <p>
     * 查询当前登录用户的个人目录，并返回其下的子目录列表和分页后的文件列表。
     * </p>
     *
     * @param userId 当前用户ID
     * @param page   当前页码（从1开始）
     * @param size   每页条数
     * @return 目录文件列表，若用户目录不存在返回null
     */
    DirectoryVO getUserDirectoryContents(Long userId, int page, int size);

    /**
     * 查看指定目录下的子目录和文件（分页）
     * <p>
     * 根据目录ID查询其下的子目录（按 sort 正序）和分页后的文件列表（按 create_time 倒序）。
     * </p>
     *
     * @param directoryId 目录ID
     * @param page        当前页码（从1开始）
     * @param size        每页条数
     * @return 目录文件列表，若目录不存在返回null
     */
    DirectoryVO getDirectoryContents(Long directoryId, int page, int size);

    /**
     * 在指定目录下创建子目录
     * <p>
     * 在当前用户的指定父目录下创建一个新的子目录。
     * </p>
     *
     * @param parentId 父目录ID
     * @param name     目录名称
     * @param userId   当前用户ID
     * @return 创建的目录
     * @throws IllegalArgumentException 父目录不存在或名称重复
     */
    FileDirectory createSubDirectory(Long parentId, String name, Long userId);

    /**
     * 获取当前用户的所有目录（平铺列表）
     * <p>
     * 用于前端移动文件时展示所有可选目录。
     * </p>
     *
     * @param userId 用户ID
     * @return 目录列表
     */
    List<FileDirectory> getAllUserDirectories(Long userId);

    /**
     * 删除文件
     * <p>
     * 将指定文件标记为已删除状态（软删除，status=0），
     * 物理文件保留磁盘以供其他引用（秒传复用）继续访问。
     * </p>
     *
     * @param fileId 文件ID
     * @param userId 当前用户ID（用于校验所有权）
     * @return true 删除成功
     * @throws IllegalArgumentException 文件不存在或无权操作
     */
    boolean deleteFile(Long fileId, Long userId);

    /**
     * 删除目录
     * <p>
     * 递归删除指定目录及其下所有文件和子目录（全部为软删除，status=0）。
     * 不允许删除根目录（parent_id=0）和用户个人目录。
     * </p>
     *
     * @param dirId  目录ID
     * @param userId 当前用户ID（用于校验所有权）
     * @return true 删除成功
     * @throws IllegalArgumentException 目录不存在或无权操作
     */
    boolean deleteDirectory(Long dirId, Long userId);

    /**
     * 在目标目录下为文件解析一个不重名的文件名
     * <p>
     * 如果目标目录中已存在同名文件，则自动生成带编号的新文件名，
     * 命名规则：原文件名 (1).扩展名、原文件名 (2).扩展名 ...
     * </p>
     *
     * @param parentId         目标目录ID
     * @param originalFileName 原始文件名
     * @return 解析后的唯一文件名（如果原始文件名不冲突则返回原文件名）
     */
    String resolveUniqueFileName(Long parentId, String originalFileName);

    /**
     * 在目标目录下为子目录解析一个不重名的目录名
     * <p>
     * 规则与 resolveUniqueFileName 一致，但查询的是 file_directory 表。
     * </p>
     *
     * @param parentId        目标父目录ID
     * @param originalDirName 原始目录名
     * @return 解析后的唯一目录名（如果原始名称不冲突则返回原名称）
     */
    String resolveUniqueDirName(Long parentId, String originalDirName);

    /**
     * 重命名文件
     * <p>
     * 将指定文件重命名为新名称。如果目标目录（文件当前所在目录）存在同名文件，则自动添加编号后缀。
     * 只有文件的上传者才能重命名。
     * </p>
     *
     * @param fileId  文件ID
     * @param newName 新文件名（含扩展名）
     * @param userId  当前用户ID
     * @return true 重命名成功
     * @throws IllegalArgumentException 文件不存在、无权操作或名称无效
     */
    boolean renameFile(Long fileId, String newName, Long userId);

    /**
     * 重命名目录
     * <p>
     * 将指定目录重命名为新名称。如果同级目录存在同名，则自动添加编号后缀。
     * 不允许重命名 id=2 的目录（User父目录）。
     * 只有目录的创建者才能重命名。
     * </p>
     *
     * @param dirId   目录ID
     * @param newName 新目录名
     * @param userId  当前用户ID
     * @return true 重命名成功
     * @throws IllegalArgumentException 目录不存在、无权操作、受保护或名称无效
     */
    boolean renameDirectory(Long dirId, String newName, Long userId);

    /**
     * 移动目录到其他目录
     * <p>
     * 将指定目录移动到目标目录下。不能移动到自身或自己的子树中。
     * 不允许移动根目录和 id=2 的 User 父目录。
     * 只有目录的创建者才能移动。
     * </p>
     *
     * @param dirId       目录ID
     * @param targetDirId 目标父目录ID
     * @param userId      当前用户ID
     * @return true 移动成功
     * @throws IllegalArgumentException 目录不存在、无权操作、移动路径非法或目标无效
     */
    boolean moveDirectory(Long dirId, Long targetDirId, Long userId);
}
