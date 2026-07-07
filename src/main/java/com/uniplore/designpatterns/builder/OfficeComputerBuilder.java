package com.uniplore.designpatterns.builder;

/**
 * 具体构建器 —— 办公版计算机
 * <p>
 * 实现 {@link ComputerBuilder} 接口，组装经济实用的办公主机
 * </p>
 *
 * @author 杨锋
 */
public class OfficeComputerBuilder implements ComputerBuilder {

    /**
     * 正在构建的产品实例
     */
    private final Computer.Builder builder;

    /**
     * 初始化办公版计算机的 Builder，使用经济型必选组件
     */
    public OfficeComputerBuilder() {
        this.builder = new Computer.Builder("Intel Core i5-13400", 16, 512);
    }

    /**
     * 构建 CPU
     *
     * @return {@link ComputerBuilder} 构造器实例
     */
    @Override
    public ComputerBuilder buildCpu() {
        System.out.println("CPU：已安装 Intel Core i5-13400");
        return this;
    }

    /**
     * 安装内存
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildMemory() {
        System.out.println("内存：已安装 16GB DDR4");
        return this;
    }

    /**
     * 安装硬盘
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildDisk() {
        System.out.println("硬盘：已安装 512GB SSD");
        return this;
    }

    /**
     * 安装显卡
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildGpu() {
        builder.gpu("集成显卡");
        System.out.println("显卡：使用集成显卡");
        return this;
    }

    /**
     * 安装操作系统
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildOperatingSystem() {
        builder.operatingSystem("Windows 11 家庭版");
        System.out.println("系统：已安装 Windows 11 家庭版");
        return this;
    }

    /**
     * 安装外设
     *
     * @return {@link ComputerBuilder} 构建器实例
     */
    @Override
    public ComputerBuilder buildPeripherals() {
        builder.hasMechanicalKeyboard(false);
        System.out.println("外设：标准键盘（非机械）");
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
