package com.uniplore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileDirectoryMapper;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.pojo.DirectoryVO;
import com.uniplore.pojo.FileDirectory;
import com.uniplore.pojo.FileInfo;
import com.uniplore.service.FileDirectoryService;
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

        FileDirectory userDir = new FileDirectory();
        userDir.setParentId(root.getId());
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
        // 检查父目录是否存在
        FileDirectory parent = getById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父目录不存在");
        }

        // 检查同级目录是否有重名
        FileDirectory exist = getOne(new QueryWrapper<FileDirectory>()
                .eq("parent_id", parentId)
                .eq("name", name)
                .eq("status", 1)
                .last("LIMIT 1")
        );
        if (exist != null) {
            throw new IllegalArgumentException("同级目录已存在同名目录");
        }

        // 创建新目录
        FileDirectory dir = new FileDirectory();
        dir.setParentId(parentId);
        dir.setName(name);
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
        // 不允许删除用户个人目录（直接隶属于根目录、且为当前用户创建）
        FileDirectory root = getRootDirectory();
        if (root != null && dir.getParentId().equals(root.getId())) {
            // 该目录是根目录下的一级子目录（即用户个人目录）
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

}
