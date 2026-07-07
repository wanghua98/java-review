package com.uniplore.designpatterns.factory.ui;

/**
 * mac 风格的按钮
 *
 * @author 杨锋
 */
public class MacButton implements Button {
    /**
     * 渲染按钮
     */
    @Override
    public void render() {
        System.out.println("渲染 Mac 风格的按钮");
    }
}

