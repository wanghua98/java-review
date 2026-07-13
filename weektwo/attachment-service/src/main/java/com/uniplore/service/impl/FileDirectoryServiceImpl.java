package com.uniplore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileDirectoryMapper;
import com.uniplore.pojo.FileDirectory;
import com.uniplore.service.FileDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 目录Service实现类
 *
 * @author yf
 */
@Service
@RequiredArgsConstructor
public class FileDirectoryServiceImpl extends ServiceImpl<FileDirectoryMapper, FileDirectory> implements FileDirectoryService {

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
}
