package com.uniplore.result;

import lombok.Data;

/**
 * 统一返回结果
 *
 * @param <T> 响应数据
 * @author 杨锋
 */
@Data
public class Result<T> {
    private final int code;
    private final String message;
    private final T data;

    /**
     * 私有构造函数
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    数据
     */
    Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 请求成功返回结果
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return Result<T>   统一返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 请求失败返回结果
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    响应数据
     * @param <T>     响应数据类型
     * @return Result<T>   统一返回结果
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

}
