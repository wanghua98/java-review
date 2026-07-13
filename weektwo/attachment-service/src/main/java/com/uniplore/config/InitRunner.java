package com.uniplore.config;

import com.uniplore.constant.RoleConstants;
import com.uniplore.mapper.UserMapper;
import com.uniplore.pojo.FileDirectory;
import com.uniplore.pojo.User;
import com.uniplore.service.FileDirectoryService;
import com.uniplore.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 系统初始化Runner
 * <p>
 * 在应用启动时自动创建ADMIN用户和根目录（如尚未初始化）。
 * </p>
 *
 * @author yf
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitRunner implements CommandLineRunner {

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;
    /**
     * 文件目录Service
     */
    private final FileDirectoryService fileDirectoryService;

    /**
     * 默认管理员用户名
     */
    private static final String DEFAULT_ADMIN_USERNAME = "admin";

    /**
     * 默认管理员密码
     */
    private static final String DEFAULT_ADMIN_PASSWORD = "aa";

    /**
     * 根目录名称
     */
    private static final String ROOT_DIRECTORY_NAME = "admin";

    @Override
    public void run(String... args) {
        // 1. 检查是否已初始化（通过查找ADMIN用户判断）
        Long adminId = getAdminUserId();
        if (adminId == null) {
            // 创建默认ADMIN用户
            User admin = new User();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPassword(PasswordUtil.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setNickname("管理员");
            admin.setRole(RoleConstants.ADMIN);
            admin.setStatus(1);
            userMapper.insert(admin);
            adminId = admin.getId();
            log.info("初始化：创建默认管理员用户（用户名：{}，密码：{}）", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        }

        // 2. 检查根目录是否存在
        FileDirectory root = fileDirectoryService.getRootDirectory();
        if (root == null) {
            FileDirectory rootDir = new FileDirectory();
            rootDir.setParentId(0L);
            rootDir.setName(ROOT_DIRECTORY_NAME);
            rootDir.setSort(0);
            rootDir.setCreateUser(adminId);
            rootDir.setStatus(1);
            fileDirectoryService.save(rootDir);
            log.info("初始化：创建根目录");
        }

        log.info("系统初始化完成");
    }

    /**
     * 获取ADMIN用户的ID
     *
     * @return ADMIN用户的ID，不存在返回null
     */
    private Long getAdminUserId() {
        User admin = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .select("id")
                        .eq("role", RoleConstants.ADMIN)
                        .last("LIMIT 1")
        );
        return admin != null ? admin.getId() : null;
    }
}
