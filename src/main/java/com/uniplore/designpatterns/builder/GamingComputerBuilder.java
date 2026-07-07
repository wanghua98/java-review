package com.uniplore.designpatterns.builder;

/**
 * 具体构建器 —— 游戏版计算机
 * 实现 {@link ComputerBuilder} 接口，组装高性能游戏主机
 *
 * @author 杨锋
 */
public class GamingComputerBuilder implements ComputerBuilder {

    /**
     * 正在构建的产品实例
     */
    private final Computer.Builder builder;

    /**
     * 初始化游戏版计算机的 Builder，
     */
    public GamingComputerBuilder() {
        this.builder = new Computer.Builder("Intel Core i13-13900K", 32, 1024);
    }

    /**
     * 构建 CPU
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildCpu() {
        // CPU 已在构造函数中初始化，无额外操作
        System.out.println("CPU：已安装 Intel Core i9-13900K");
        return this;
    }

    @Override
    public ComputerBuilder buildMemory() {
        // 内存已在构造函数中初始化
        System.out.println("内存：已安装 32GB DDR5");
        return this;
    }

    @Override
    public ComputerBuilder buildDisk() {
        // 硬盘已在构造函数中初始化
        System.out.println("硬盘：已安装 1TB NVMe SSD");
        return this;
    }

    /**
     * 构建显卡
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildGpu() {
        builder.gpu("RTX 5090");
        System.out.println("显卡：已安装 RTX 5090");
        return this;
    }

    /**
     * 构建操作系统
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildOperatingSystem() {
        builder.operatingSystem("Windows 11 专业版");
        System.out.println("系统：已安装 Windows 11 专业版");
        return this;
    }

    /**
     * 构建外设
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildPeripherals() {
        builder.hasMechanicalKeyboard(true);
        System.out.println("外设：已配备机械键盘");
        return this;
    }

    /**
     * 获取产品实例
     *
     * @return {@link Computer} 产品实例
     */
    @Override
    public Computer build() {
        return builder.build();
    }
}
