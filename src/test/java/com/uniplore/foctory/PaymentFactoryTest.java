package com.uniplore.foctory;

import com.uniplore.designpatterns.factory.AlipayFactory;
import com.uniplore.designpatterns.factory.Payment;
import com.uniplore.designpatterns.factory.PaymentFactory;
import com.uniplore.designpatterns.factory.WechatPayFactory;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * 工厂方法测试
 *
 * @author 杨锋
 */
public class PaymentFactoryTest {

    /**
     * 测试创建支付对象
     */
    @Test
    public void testCreatePayment() {

        // 创建支付宝支付对象
        PaymentFactory paymentFactory = new AlipayFactory();
        Payment payment = paymentFactory.createPayment();
        payment.pay(new BigDecimal("100"));

        // 创建微信支付对象
        paymentFactory = new WechatPayFactory();
        payment = paymentFactory.createPayment();
        payment.pay(new BigDecimal("100"));

    }
}
