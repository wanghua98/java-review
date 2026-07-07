package com.uniplore.featuresofJava8;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class Main {

    public static void main(String[] args) {

        /*
          Lambda  (parameters)->expression
          可选类型声明：不需要声明参数类型，编译器可以统一识别参数值。
          可选的参数圆括号：一个参数无需定义圆括号，但多个参数需要定义圆括号。
          可选的大括号：如果主体包含了一个语句，就不需要使用大括号。
          可选的返回关键字：如果主体只有一个表达式返回值则编译器会自动返回值，大括号需要指定明表达式返回了一个数值。
         */


        /**
         * // 1. 不需要参数,返回值为 5
         * () -> 5
         *
         * // 2. 接收一个参数(数字类型),返回其2倍的值
         * x -> 2 * x
         *
         * // 3. 接受2个参数(数字),并返回他们的差值
         * (x, y) -> x – y
         *
         * // 4. 接收2个int型整数,返回他们的和
         * (int x, int y) -> x + y
         *
         * // 5. 接受一个 string 对象,并在控制台打印,不返回任何值(看起来像是返回void)
         * (String s) -> System.out.print(s)
         */
        Main tester = new Main();
        // 类型声明
        MathOperation addition = (int a, int b) -> a + b;
        MathOperation add = new MathOperation() {
            @Override
            public int operation(int a, int b) {
                return a + b;
            }
        };
        // 不用类型声明
        MathOperation subtraction = (a, b) -> a - b;
        // 大括号中的返回语句
        MathOperation multiplication = (int a, int b) -> {
            return a * b;
        };

        // 没有大括号及返回语句
        MathOperation division = (int a, int b) -> a / b;

        System.out.println("10 + 5 = " + tester.operate(10, 5, addition));
        System.out.println("10 - 5 = " + tester.operate(10, 5, subtraction));
        System.out.println("10 x 5 = " + tester.operate(10, 5, multiplication));
        System.out.println("10 / 5 = " + tester.operate(10, 5, division));

        // 不用括号
        GreetingService greetService1 = message ->
                System.out.println("Hello " + message);

        // 用括号
        GreetingService greetService2 = (message) ->
                System.out.println("Hello " + message);

        greetService1.sayMessage("Runoob");
        greetService2.sayMessage("Google");


    }

    /**
     * Lambda 表达式
     *
     */
    interface MathOperation {
        int operation(int a, int b);

    }

    /**
     * 函数式接口
     *
     */
    interface GreetingService {
        void sayMessage(String message);
    }

    /**
     * Lambda表达式在线程创建中使用
     */
    @Test
    public void test1() {
        var t = new Thread(r);
        t.start();

        //等待线程t结束
        try {
            t.join();
        } catch (InterruptedException e) {
            System.out.println("com.uniplore.Main thread interrupted.");
        }
    }

    Runnable r = () -> {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                System.out.println("Thread interrupted.");
            }
        }
    };

    private int operate(int a, int b, MathOperation mathOperation) {
        return mathOperation.operation(a, b);
    }

    /**
     * 方法引用 个人感觉像c++的作用域符号
     * 类型	         语法	            对应的Lambda表达式
     * 静态方法引用	类名::staticMethod	(args) -> 类名.staticMethod(args)
     * 实例方法引用	inst::instMethod	(args) -> inst.instMethod(args)
     * 对象方法引用	类名::instMethod	    (inst,args) -> 类名.instMethod(args)
     * 构建方法引用	类名::new	        (args) -> new 类名(args)
     */
    @Test
    public void test() {
        List<String> names = new ArrayList<>();

        names.add("Google");
        names.add("Runoob");
        names.add("Taobao");
        names.add("Baidu");
        names.add("Sina");

        names.forEach(System.out::println);

    }

    /**
     * Java 8 API添加了一个新的抽象称为流Stream，可以让你以一种声明的方式处理数据。
     * Stream 使用一种类似用 SQL 语句从数据库查询数据的直观方式来提供一种对 Java 集合运算和表达的高阶抽象。
     * Stream API可以极大提高Java程序员的生产力，让程序员写出高效率、干净、简洁的代码。
     * 这种风格将要处理的元素集合看作一种流， 流在管道中传输， 并且可以在管道的节点上进行处理， 比如筛选， 排序，聚合等。
     * 元素流在管道中经过中间操作（intermediate operation）的处理，最后由最终操作(terminal operation)得到前面处理的结果。
     *
     */
    @Test
    public void teststream() {
        List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd", "", "jkl");
        List<String> filtered = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());
        filtered.forEach(System.out::println);

        //limit 方法用于获取指定数量的流。 以下代码片段使用 limit 方法打印出 10 条数据：
        //    sorted 方法用于对流进行排序。以下代码片段使用 sorted 方法对输出的 10 个随机数进行排序：
        Random random = new Random();
        random.ints().limit(10).forEach(System.out::println);
        //map 方法用于映射每个元素到对应的结果，以下代码片段使用 map 输出了元素对应的平方数：
        List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);
        // 获取对应的平方数
        List<Integer> squaresList = numbers.stream().map(i -> i * i).distinct().collect(Collectors.toList());
        squaresList.forEach(System.out::println);

        //filter 方法用于通过设置的条件过滤出元素。以下代码片段使用 filter 方法过滤出空字符串：
        List<String> Strings = Arrays.asList("abc", "", "bc", "efg", "abcd", "", "jkl");
        // 获取空字符串的数量
        long count = Strings.stream().filter(string -> string.isEmpty()).count();
        //parallelStream 是流并行处理程序的代替方法。以下实例我们使用 parallelStream 来输出空字符串的数量：
        List<String> str = Arrays.asList("abc", "", "bc", "efg", "abcd", "", "jkl");
        // 获取空字符串的数量
        int number = (int) str.parallelStream().filter(string -> string.isEmpty()).count();
//      Collectors 类实现了很多归约操作，例如将流转换成集合和聚合元素。Collectors 可用于返回列表或字符串：
//    List<String>strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
//    List<String> filtered = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());
//
//    System.out.println("筛选列表: " + filtered);
//    String mergedString = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.joining(", "));
//    System.out.println("合并字符串: " + mergedString);


//    另外，一些产生统计结果的收集器也非常有用。它们主要用于int、double、long等基本类型上，它们可以用来产生类似如下的统计结果。

//    List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);
//
//    IntSummaryStatiœœstics stats = numbers.stream().mapToInt((x) -> x).summaryStatistics();
//
//    System.out.println("列表中最大的数 : " + stats.getMax());
//    System.out.println("列表中最小的数 : " + stats.getMin());
//    System.out.println("所有数之和 : " + stats.getSum());
//    System.out.println("平均数 : " + stats.getAverage());

    }


}