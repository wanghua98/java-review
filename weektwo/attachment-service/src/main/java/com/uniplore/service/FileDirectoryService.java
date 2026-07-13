package com.uniplore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.FileDirectory;

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
     * @param userId   用户ID
     * @param newName  新目录名称
     * @return 更新后的目录
     */
    FileDirectory renameUserDirectory(Long userId, String newName);
}
