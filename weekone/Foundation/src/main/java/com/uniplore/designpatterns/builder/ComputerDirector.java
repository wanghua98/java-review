package com.uniplore.designpatterns.builder;

/**
 * 指挥者 —— 控制构建过程
 * 负责按固定顺序调用构建器的各个步骤，确保构建过程一致。
 * 客户端无需关心具体的装配流程，只需选择使用哪种构建器。
 *
 * @author 杨锋
 */
public class ComputerDirector {

    /**
     * 构建器实例
     */
    private final ComputerBuilder builder;

    /**
     * 构造指挥者，指定使用哪一种构建器
     *
     * @param builder 构建器决定了产品的具体配置
     */
    public ComputerDirector(ComputerBuilder builder) {
        this.builder = builder;
    }

    /**
     * 构造计算机
     * 按固定顺序调用构建器的各个步骤，完成一台计算机的组装。
     * 顺序不可变：CPU → 内存 → 硬盘 → 显卡 → 系统 → 外设。
     *
     * @return 组装完成的 Computer 对象
     */
    public Computer construct() {
        return builder
                .buildCpu()
                .buildMemory()
                .buildDisk()
                .buildGpu()
                .buildOperatingSystem()
                .buildPeripherals()
                .build();
    }
}
