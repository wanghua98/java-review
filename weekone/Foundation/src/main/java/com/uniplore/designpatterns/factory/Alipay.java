package com.uniplore.designpatterns.factory;

import java.math.BigDecimal;

/**
 * 支付宝支付
 *
 * @author 杨锋
 */
public class Alipay implements Payment {
    /**
     * 支付方法
     *
     * @param amount 金额
     */
    @Override
    public void pay(BigDecimal amount) {
        System.out.println("使用支付宝支付：" + amount + " 元");
    }

}

