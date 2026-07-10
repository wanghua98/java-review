package com.uniplore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.User;
import com.uniplore.pojo.UserDTO;
import com.uniplore.result.Result;


/**
 * 用户Service接口
 *
 * @author yf
 */
public interface UserService extends IService<User> {
    /**
     * 用户登陆
     *
     * @param userDTO 用户DTO
     * @return 结果
     */
    Result<String> doLogin(UserDTO userDTO);

    /**
     * 用户注册
     *
     * @param userDTO 用户DTO
     * @return 结果
     */
    Result<String> register(UserDTO userDTO);

    /**
     * 修改用户信息
     *
     * @param userDTO 用户DTO
     * @return 结果
     */
    Result<String> changeInfo(UserDTO userDTO);
}
