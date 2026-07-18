package com.uniplore.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.uniplore.config.LogRecord;
import com.uniplore.pojo.UserDTO;
import com.uniplore.pojo.UserVO;
import com.uniplore.result.Result;
import com.uniplore.result.ResultMessage;
import com.uniplore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author yf
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登陆接口
     *
     * @param userDTO 用户DTO传入用户名以及密码
     * @return 返回登陆结果
     */
    @LogRecord("用户登录")
    @PostMapping("/login")
    public Result<String> doLogin(@RequestBody UserDTO userDTO) {

        // 获取用户名和密码
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        // 进行参数校验
        if (username == null || password == null) {
            return Result.error(400, ResultMessage.INVALID_PARAMETERS.getMessage(), null);
        }


        // 返回结果
        return userService.doLogin(userDTO);
    }

    /**
     * 账号登出接口
     *
     * @return 登出结果
     */
    @LogRecord("用户登出")
    @GetMapping("/logout")
    public Result<String> doLogout() {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        StpUtil.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @LogRecord("获取用户信息")
    @GetMapping("/info")
    public Result<UserVO> getInfo() {
        // 判断用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        // 获取用户信息
        UserVO userVO = BeanUtil.copyProperties(userService.getById(StpUtil.getLoginIdAsLong()), UserVO.class);
        // 修改密码字段
        userVO.setPassword("******");
        return Result.success(userVO);
    }

    /**
     * 注册并登陆
     *
     * @param userDTO 用户DTO传入用户名以及密码
     * @return 注册结果
     */
    @LogRecord("用户注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    /**
     * 修改用户信息
     *
     * @param userDTO 用户DTO传入用户名以及密码
     * @return 修改结果
     */
    @LogRecord("修改用户信息")
    @PostMapping("/changeInfo")
    public Result<String> changeInfo(@RequestBody UserDTO userDTO) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        return userService.changeInfo(userDTO);
    }
}
