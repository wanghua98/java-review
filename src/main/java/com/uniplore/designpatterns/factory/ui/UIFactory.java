package com.uniplore.designpatterns.factory.ui;

import com.uniplore.designpatterns.factory.TextField;

/**
 * UI抽象工厂类
 *
 * @author 杨锋
 */
public interface UIFactory {
    /**
     * 创建按钮
     *
     * @return 按钮对象
     */
    Button createButton();

    /**
     * 创建文本字段
     *
     * @return 文本字段对象
     */
    TextField createTextField();
}
