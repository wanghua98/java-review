package com.uniplore.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.UserMapper;
import com.uniplore.pojo.User;
import com.uniplore.pojo.UserDTO;
import com.uniplore.result.Result;
import com.uniplore.service.UserService;
import com.uniplore.util.PasswordUtil;
import org.springframework.stereotype.Service;

/**
 * 用户实现类
 *
 * @author yf
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户登陆
     *
     * @param userDTO 用户DTO
     * @return 结果
     */
    @Override
    public Result<String> doLogin(UserDTO userDTO) {
        // 比对前端提交的账号名称
        User user = query().select("id", "username", "password", "role").eq("username", userDTO.getUsername()).one();

        // 如果用户不存在，则返回错误
        if (user == null) {
            return Result.error(400, "账号密码错误", null);
        }
        // 进行密码比对
        boolean matches = PasswordUtil.matches(userDTO.getPassword(), user.getPassword());
        if (!matches) {
            return Result.error(400, "账号密码错误", null);
        }

        // 获取用户ID
        Long id = user.getId();
        if (id == null) {
            return Result.error(500, "用户ID为空,请联系管理员", null);
        }
        // 登录并在session中保存用户信息
        StpUtil.login(id);

        return Result.success("登录成功");
    }

    /**
     * 用户注册
     *
     * @param userDTO 用户DTO
     * @return 结果
     */
    @Override
    public Result<String> register(UserDTO userDTO) {

        // 比对前端提交的账号名称
        User user = query().select("id").eq("username", userDTO.getUsername()).one();
        if (user != null) {
            return Result.error(400, "账号已存在", null);
        }
        // 创建用户对象并设置属性
        user = BeanUtil.copyProperties(userDTO, User.class);
        user.setPassword(PasswordUtil.encode(userDTO.getPassword()));
        user.setRole("USER");
        user.setNickname("user_" + RandomUtil.randomNumbers(5));
        save(user);

        // 登陆
        StpUtil.login(user.getId());
        return Result.success("注册成功");
    }

    /**
     * 修改用户信息
     *
     * @param userDTO 用户DTO
     * @return 结果
     */
    @Override
    public Result<String> changeInfo(UserDTO userDTO) {

        // 设置用户ID
        userDTO.setId(StpUtil.getLoginIdAsLong());

        //判断用户名是否重复
        if (userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
            User user = query().select("username").eq("username", userDTO.getUsername()).one();
            if (user != null) {
                return Result.error(400, "用户名已存在", null);
            }
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            userDTO.setPassword(PasswordUtil.encode(userDTO.getPassword()));
        }
        // 更新用户信息
        updateById(BeanUtil.copyProperties(userDTO, User.class));
        return Result.success("修改成功");

    }
}
