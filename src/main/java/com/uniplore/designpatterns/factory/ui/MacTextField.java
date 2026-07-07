package com.uniplore.designpatterns.factory.ui;

import com.uniplore.designpatterns.factory.TextField;

/**
 * mac 风格的文本框
 *
 * @author 杨锋
 */
public class MacTextField implements TextField {
    /**
     * 渲染文本框
     */
    @Override
    public void render() {
        System.out.println("渲染 Mac 风格的文本框");
    }
}
