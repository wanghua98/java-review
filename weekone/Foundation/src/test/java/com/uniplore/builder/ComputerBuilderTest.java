package com.uniplore.builder;

import com.uniplore.designpatterns.builder.Computer;
import com.uniplore.designpatterns.builder.ComputerDirector;
import com.uniplore.designpatterns.builder.GamingComputerBuilder;
import com.uniplore.designpatterns.builder.OfficeComputerBuilder;
import org.junit.Test;

/**
 * 建造者模式测试类
 *
 * @author 杨锋
 */
public class ComputerBuilderTest {

    /**
     * 测试客户端直接使用静态 Builder 构建对象
     */
    @Test
    public void testDirectBuilder() {
        /*
          使用静态 Builder 链式调用构建一台游戏主机
         */
        Computer gaming = new Computer.Builder("Intel Core i9-13900K", 32, 1024).gpu("NVIDIA GeForce RTX 5090").operatingSystem("Windows 11 专业版").hasMechanicalKeyboard(true).build();
        System.out.println(gaming);

        /*
          使用静态 Builder 构建一台办公主机（只指定必选参数）
         */
        Computer office = new Computer.Builder("Intel Core i5-13400", 16, 512).build();
        System.out.println(office);
    }

    /**
     * 测试 Director + Builder 方式构建游戏版计算机
     */
    @Test
    public void testDirectorWithGamingBuilder() {
        // 创建指挥者，指定使用游戏版构建器
        ComputerDirector director = new ComputerDirector(new GamingComputerBuilder());
        Computer gaming = director.construct();

        System.out.println("游戏版计算机配置：");
        System.out.println(gaming);
    }

    /**
     * 测试Director + Builder方式构建办公版计算机
     */
    @Test
    public void testDirectorWithOfficeBuilder() {
        // 创建指挥者，指定使用办公版构建器
        ComputerDirector director = new ComputerDirector(new OfficeComputerBuilder());
        Computer office = director.construct();

        System.out.println("办公版计算机配置：");
        System.out.println(office);
    }

}
