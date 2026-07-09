package com.uniplore.designpatterns.factory.ui;

import com.uniplore.designpatterns.factory.TextField;

/**
 * Mac 风格的 UI 工厂类
 *
 * @author 杨锋
 */
public class MacUIFactory implements UIFactory {
    /**
     * 创建 Mac 风格的按钮
     * @return 按钮对象
     */
    @Override
    public Button createButton() {
        return new MacButton();
    }

    /**
     * 创建 Mac 风格的文本字段
     * @return 文本字段对象
     */
    @Override
    public TextField createTextField() {
        return new MacTextField();
    }
}
