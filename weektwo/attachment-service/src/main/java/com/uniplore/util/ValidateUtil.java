package com.uniplore.util;

import com.uniplore.result.ResultMessage;

/**
 * 输入校验工具类
 * <p>
 * 提供用户名、密码、目录名称等通用校验方法。
 * 校验不通过时抛出 IllegalArgumentException，由上层统一捕获处理。
 * 错误消息统一使用 {@link ResultMessage} 中的常量。
 * </p>
 *
 * @author yf
 */
public class ValidateUtil {

    /** 文件名/目录名非法字符 */
    private static final String ILLEGAL_CHARS = "\\\\/:*?\"<>|";

    private ValidateUtil() {
        // 工具类禁止实例化
    }

    /**
     * 校验用户名格式
     * <p>
     * 规则：只能包含字母和数字，必须以字母开头，长度 1~50。
     * </p>
     *
     * @param username 用户名
     * @throws IllegalArgumentException 格式不合法时抛出
     */
    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(ResultMessage.USERNAME_EMPTY.getMessage());
        }
        String trimmed = username.trim();
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException(ResultMessage.USERNAME_TOO_LONG.getMessage());
        }
        if (!Character.isLetter(trimmed.charAt(0))) {
            throw new IllegalArgumentException(ResultMessage.USERNAME_MUST_START_WITH_LETTER.getMessage());
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                throw new IllegalArgumentException(ResultMessage.USERNAME_INVALID_CHARS.getMessage());
            }
        }
    }

    /**
     * 校验密码长度
     * <p>
     * 规则：长度 6~100。
     * </p>
     *
     * @param password 密码
     * @throws IllegalArgumentException 格式不合法时抛出
     */
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(ResultMessage.PASSWORD_EMPTY.getMessage());
        }
        if (password.length() < 2) {
            throw new IllegalArgumentException(ResultMessage.PASSWORD_TOO_SHORT.getMessage());
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException(ResultMessage.PASSWORD_TOO_LONG.getMessage());
        }
    }

    /**
     * 校验目录名称
     * <p>
     * 规则：不能为空，不能包含非法字符（\ / : * ? " &lt; &gt; |），
     * 长度 1~100。
     * </p>
     *
     * @param name 目录名称
     * @throws IllegalArgumentException 名称不合法时抛出
     */
    public static void validateDirectoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(ResultMessage.DIR_NAME_EMPTY.getMessage());
        }
        String trimmed = name.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException(ResultMessage.DIR_NAME_TOO_LONG.getMessage());
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (ILLEGAL_CHARS.indexOf(c) >= 0) {
                throw new IllegalArgumentException(
                        ResultMessage.DIR_NAME_INVALID_CHARS.getMessage() + c);
            }
        }
    }

    /**
     * 校验文件名
     * <p>
     * 规则：不能为空，不能包含非法字符（\ / : * ? " &lt; &gt; |），
     * 长度 1~255。
     * </p>
     *
     * @param fileName 文件名称
     * @throws IllegalArgumentException 名称不合法时抛出
     */
    public static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(ResultMessage.FILE_NAME_EMPTY.getMessage());
        }
        String trimmed = fileName.trim();
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException(ResultMessage.FILE_NAME_TOO_LONG.getMessage());
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (ILLEGAL_CHARS.indexOf(c) >= 0) {
                throw new IllegalArgumentException(
                        ResultMessage.FILE_NAME_INVALID_CHARS.getMessage() + c);
            }
        }
    }
}
