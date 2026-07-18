package com.uniplore.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * @author yf
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRecord {

    /**
     * 操作描述
     * <p>例如：用户登录、查询文件列表。</p>
     *
     * @return 操作描述
     */
    String value() default "";
}
