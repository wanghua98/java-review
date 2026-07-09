package com.uniplore.designpatterns.factory;

/**
 * 支付工厂接口
 *
 * @author 杨锋
 */
public interface PaymentFactory {
    /**
     * 创建支付对象
     *
     * @return 支付对象
     */
    Payment createPayment();
}
