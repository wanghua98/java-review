package com.uniplore.designpatterns.builder;

/**
 * Builder 模式 —— 抽象构建器接口
 * <p>
 * 定义构建一个复杂产品（Computer）所需的各个步骤，
 * 具体实现类负责组装不同配置的计算机。
 * </p>
 *
 * @author 杨锋
 */
public interface ComputerBuilder {

    /**
     * 安装 CPU
     *
     * @return 当前 Builder 实例，便于链式调用
     */
    ComputerBuilder buildCpu();

    /**
     * 安装内存
     *
     * @return 当前 Builder 实例，便于链式调用
     */
    ComputerBuilder buildMemory();

    /**
     * 安装硬盘
     *
     * @return 当前 Builder 实例，便于链式调用
     */
    ComputerBuilder buildDisk();

    /**
     * 安装显卡
     *
     * @return 当前 Builder 实例，便于链式调用
     */
    ComputerBuilder buildGpu();

    /**
     * 安装操作系统
     *
     * @return 当前 Builder 实例，便于链式调用
     */
    ComputerBuilder buildOperatingSystem();

    /**
     * 装配外设
     *
     * @return 当前 Builder 实例，便于链式调用
     */
    ComputerBuilder buildPeripherals();

    /**
     * 获取最终构建的产品
     *
     * @return 构建完成的 Computer 实例
     */
    Computer build();
}
