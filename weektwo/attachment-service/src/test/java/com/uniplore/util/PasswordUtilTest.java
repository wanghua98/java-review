package com.uniplore.util;


import org.junit.jupiter.api.Test;

public class PasswordUtilTest {

    @Test
    public void testPasswordUtil() {
        String password = "aa";
        String encodedPassword = PasswordUtil.encode(password);
        System.out.println("aa加密后的密码为：" + encodedPassword);
        boolean matches = PasswordUtil.matches(password, encodedPassword);
        System.out.println(matches);
    }
}
