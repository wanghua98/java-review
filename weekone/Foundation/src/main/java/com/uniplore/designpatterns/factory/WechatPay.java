package com.uniplore.designpatterns.factory;

import java.math.BigDecimal;

/**
 * 微信支付
 *
 * @author 杨锋
 */
public class WechatPay implements Payment {
    @Override
    public void pay(BigDecimal amount) {
        System.out.println("使用微信支付：" + amount + " 元");
    }
}
