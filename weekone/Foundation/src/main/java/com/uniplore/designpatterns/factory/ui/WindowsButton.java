package com.uniplore.designpatterns.factory.ui;

/**
 * Windows 风格的按钮
 *
 * @author 杨锋
 */
public class WindowsButton implements Button {
    /**
     * 渲染按钮
     *
     * @author 杨锋
     */
    @Override
    public void render() {
        System.out.println("渲染 Windows 风格的按钮");
    }
}

