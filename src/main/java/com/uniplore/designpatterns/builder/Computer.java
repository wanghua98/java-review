package com.uniplore.designpatterns.builder;

import lombok.Getter;

/**
 * 产品类 —— 计算机<br/>
 * 由 Builder 模式一步步构建的复杂对象，包含 CPU、内存、硬盘、显卡等组件。
 *
 * @author 杨锋
 */
public class Computer {
    /**
     * CPU 型号
     * 获取 CPU 型号
     */
    private final String cpu;

    /**
     * 内存大小（GB）
     */
    private final int memory;

    /**
     * 硬盘大小（GB）
     */
    private final int disk;

    /**
     * 显卡型号（可选）
     */
    private final String gpu;

    /**
     * 操作系统（可选）
     */
    private final String operatingSystem;

    /**
     * 是否配备机械键盘
     */
    private final boolean hasMechanicalKeyboard;

    /**
     * 私有构造函数，只能通过 Builder 创建
     *
     * @param builder 构建器
     */
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.memory = builder.memory;
        this.disk = builder.disk;
        this.gpu = builder.gpu;
        this.operatingSystem = builder.operatingSystem;
        this.hasMechanicalKeyboard = builder.hasMechanicalKeyboard;
    }


    public boolean hasMechanicalKeyboard() {
        return hasMechanicalKeyboard;
    }

    /**
     * 静态内部 Builder 类 —— 一步步构建 Computer 对象
     * 通过链式调用设置各组件，最后调用 {@link #build()} 生成产品。
     *
     */
    public static class Builder {
        // 必选参数
        private final String cpu;
        private final int memory;
        private final int disk;

        // 可选参数
        private String gpu = "集成显卡";
        private String operatingSystem = "未安装";
        private boolean hasMechanicalKeyboard = false;

        /**
         * 构造 Builder，传入必选参数
         *
         * @param cpu    CPU 型号
         * @param memory 内存大小（GB）
         * @param disk   硬盘大小（GB）
         */
        public Builder(String cpu, int memory, int disk) {
            this.cpu = cpu;
            this.memory = memory;
            this.disk = disk;
        }

        /**
         * 设置显卡
         *
         * @param gpu 显卡型号
         * @return 当前 Builder 实例，便于链式调用
         */
        public Builder gpu(String gpu) {
            this.gpu = gpu;
            return this;
        }

        /**
         * 设置操作系统
         *
         * @param operatingSystem 操作系统名称
         * @return 当前 Builder 实例，便于链式调用
         */
        public Builder operatingSystem(String operatingSystem) {
            this.operatingSystem = operatingSystem;
            return this;
        }

        /**
         * 设置是否配备机械键盘
         *
         * @param hasMechanicalKeyboard 是否配备
         * @return 当前 Builder 实例，便于链式调用
         */
        public Builder hasMechanicalKeyboard(boolean hasMechanicalKeyboard) {
            this.hasMechanicalKeyboard = hasMechanicalKeyboard;
            return this;
        }

        /**
         * 构建最终的 Computer 对象
         *
         * @return 构建完成的 Computer 实例
         */
        public Computer build() {
            return new Computer(this);
        }
    }

    /**
     * 重写 toString() 方法，方便打印 Computer 对象
     *
     * @return Computer 对象的字符串表示
     */
    @Override
    public String toString() {
        return "Computer{" +
                "cpu='" + cpu + '\'' +
                ", memory=" + memory + "GB" +
                ", disk=" + disk + "GB" +
                ", gpu='" + gpu + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", hasMechanicalKeyboard=" + hasMechanicalKeyboard +
                '}';
    }
}
