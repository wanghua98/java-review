package com.uniplore.designpatterns.factory.ui;

import com.uniplore.designpatterns.factory.TextField;

/**
 * Windows 风格的文本框
 *
 * @author 杨锋
 */
public class WindowsTextField implements TextField {

    /**
     * 渲染文本框
     */
    @Override
    public void render() {
        System.out.println("渲染 Windows 风格的文本框");
    }
}
