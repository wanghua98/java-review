package com.uniplore.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务工具类
 *
 * @author yf
 */
public class AfterCommitRunner {
    /**
     * 当前事务提交后执行（无事务则直接执行）
     */
    public static void afterCommit(Runnable runnable) {
        // 如果当前事务活跃，注册事务提交后执行
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            runnable.run();
                        }
                    });
        } else {
            runnable.run();
        }
    }
}