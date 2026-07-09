package com.uniplore.designpatterns.factory;

import java.math.BigDecimal;

/**
 * 支付接口
 *
 * @author 杨锋
 */
public interface Payment {
    /**
     * 支付方法
     *
     * @param amount 金额
     */
    void pay(BigDecimal amount);
}