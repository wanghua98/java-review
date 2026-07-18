package com.uniplore.config;

import cn.dev33.satoken.stp.StpUtil;
import com.uniplore.mapper.FileOperationLogMapper;
import com.uniplore.pojo.FileOperationLog;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


/**
 * 操作日志切面
 * <p>
 * 拦截 {@link LogRecord} 注解，自动记录用户操作到 file_operation_log 表。
 * </p>
 *
 * @author yf
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final FileOperationLogMapper fileOperationLogMapper;

    /**
     * 环绕通知：方法执行前后记录日志
     *
     * @param joinPoint 连接点
     * @param logRecord 日志注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(logRecord)")
    public Object around(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        // 执行目标方法
        Object result = joinPoint.proceed();

        // 记录操作日志
        FileOperationLog log = new FileOperationLog();
        log.setUserId(getCurrentUserId());
        log.setOperationType("API");
        log.setDescription(logRecord.value());
        fileOperationLogMapper.insert(log);


        return result;
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未登录返回 0
     */
    private Long getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            // 忽略异常，未登录场景 userId 为 0
        }
        return 0L;
    }
}
