package com.uniplore.foctory;

import com.uniplore.designpatterns.factory.Payment;
import com.uniplore.designpatterns.factory.SimplePaymentFactory;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * 简单支付工厂类测试
 *
 * @author 杨锋
 */
public class SimplePaymentFactoryTest {

    /**
     * 测试创建支付对象方法
     */
    @Test
    public void testCreatePayment() {
        // 创建支付宝支付对象并调用支付方法
        Payment alipay = SimplePaymentFactory.createPayment("alipay");
        alipay.pay(new BigDecimal("100"));
        // 创建微信支付对象并调用支付方法
        Payment wechat = SimplePaymentFactory.createPayment("wechat");
        wechat.pay(new BigDecimal("100"));

    }
}
