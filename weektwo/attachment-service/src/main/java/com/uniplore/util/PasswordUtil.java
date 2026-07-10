package com.uniplore.util;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码工具类用于验证密码是否正确
 *
 * @author yf
 */
public class PasswordUtil {

    /**
     * 自定义密钥
     */
    private static final String PEPPER = "uniplore";

    /**
     * 对密码进行加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    public static String encode(String password) {
        return DigestUtil.bcrypt(PEPPER + password);
    }

    /**
     * 验证密码是否正确
     *
     * @param password       明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否正确
     */
    public static boolean matches(String password, String encodedPassword) {

        return DigestUtil.bcryptCheck(PEPPER + password, encodedPassword);
    }
}
