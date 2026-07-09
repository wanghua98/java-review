package com.uniplore.designpatterns.factory;

/**
 * 支付宝支付工厂类
 *
 * @author 杨锋
 */
public class AlipayFactory implements PaymentFactory {
    /**
     * 创建支付宝支付对象
     * @return 支付宝支付对象
     */
    @Override
    public Payment createPayment() {
        return new Alipay();
    }
}
