package com.uniplore.designpatterns.factory;

/**
 * 简单支付工厂类
 *
 * @author 杨锋
 */
public class SimplePaymentFactory {

    /**
     * 根据支付类型创建支付对象
     *
     * @param type 支付类型
     * @return 支付对象
     */
    public static Payment createPayment(String type) {
        // 参数考验
        if (type == null) {
            return null;
        }
        // 创建支付对象
        return switch (type.toLowerCase()) {
            case "alipay" -> new Alipay();
            case "wechat" -> new WechatPay();
            default -> throw new IllegalArgumentException("不支持的支付类型：" + type);
        };
    }
}

