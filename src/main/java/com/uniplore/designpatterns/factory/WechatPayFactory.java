package com.uniplore.designpatterns.factory;

/**
 * 微信支付工厂类
 *
 * @author 杨锋
 */
public class WechatPayFactory implements PaymentFactory {
    /**
     * 创建微信支付对象
     *
     * @return 微信支付对象
     */
    @Override
    public Payment createPayment() {
        return new WechatPay();
    }
}
