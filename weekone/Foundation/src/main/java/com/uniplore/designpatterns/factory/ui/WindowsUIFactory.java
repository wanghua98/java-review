package com.uniplore.designpatterns.factory.ui;

import com.uniplore.designpatterns.factory.TextField;

/**
 * Windows 风格的 UI 工厂类
 *
 * @author 杨锋
 */
public class WindowsUIFactory implements UIFactory {

    /**
     * 创建 Windows 风格的按钮
     *
     * @return Windows 按钮对象
     */
    @Override
    public Button createButton() {
        return new WindowsButton();
    }

    /**
     * 创建 Windows 风格的文本字段
     *
     * @return Windows 文本框对象
     */
    @Override
    public TextField createTextField() {
        return new WindowsTextField();
    }
}
