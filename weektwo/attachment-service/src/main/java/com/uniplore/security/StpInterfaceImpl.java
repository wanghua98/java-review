package com.uniplore.security;

import cn.dev33.satoken.stp.StpInterface;
import com.uniplore.pojo.User;
import com.uniplore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author yf
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    /**
     * 用于查询用户权限的service
     */
    private final UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     * 由于我们权限只有角色区分所以暂时返回空值
     *
     * @param loginId   账号ID
     * @param loginType 账号登陆类型
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // list是用户的权限list
        return new ArrayList<String>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     *
     * @param loginId   账号ID
     * @param loginType 账号登陆类型
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // list是用户的权限list
        List<String> list = new ArrayList<String>();

        //查询用户权限
        User user = userService.query().eq("id", loginId).one();
        System.out.println(user.getRole());

        // 将用户权限添加到list
        list.add(user.getRole());

        // 可以根据用户id查询用户权限
        return list;
    }

}
