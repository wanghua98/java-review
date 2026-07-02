import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


//用于过java基础语法
public class Main {
    public static void main(String[] args) throws IOException {
        //基本数据类型
        byte g = 10; //1字节
        short h = 10; //2字节
        int a = 10; //4字节
        long b = 10L; //8字节
        float c = 10.0f;//4字节
        double d = 10.0;//8字节
        char e = 'a'; //2字节
        boolean f = true; //1字节
        //输出验证
        System.out.println("g:" + g + " h:" + h + " a:" + a + " b:" + b + " c:" + c + " d:" + d + " e:" + e + " f:" + f);

        //变量与常量以上均为变量，常量如下形式 或者类常量这里不做展示
        //其他类可以通过类名.常量名 的方式使用这个类常量
        final double CM_PER_INCH = 2.54;
        System.out.println("CM_PER_INCH:" + CM_PER_INCH);

        //枚举类型
        enum Size { SMALL, MEDIUM, LARGE, EXTRA_LARGE };
        Size s = Size.MEDIUM;
        System.out.println("s:" + s);

        //通常的算术运算符 + - * / %过于简单这里不做展示
        //Math常用函数如下，三角函数、对数函数、指数函数不列举
        System.out.println("Math.max(10, 20):" + Math.max(10, 20));
        System.out.println("Math.min(10, 20):" + Math.min(10, 20));
        System.out.println("Math.sqrt(10.0):" + Math.sqrt(10.0));
        System.out.println("Math.pow(2.0, 3.0):" + Math.pow(2.0, 3.0));  //求2的3次方
        System.out.println("Math.round(10.0):" + Math.round(10.0)); //四舍五入
        System.out.println("Math.round(10.5):" + Math.round(10.5));
        System.out.println("Math.ceil(10.0):" + Math.ceil(10.0)); //Math.ceil()向上取整
        System.out.println("Math.floor(10.0):" + Math.floor(10.0)); //向下取整
        System.out.println("Math.PI:" + Math.PI);
        System.out.println("Math.E:" + Math.E);
        System.out.println("Math.abs(-10.0):" + Math.abs(-10.0)); //取绝对值


        //数值类型之间的转换
        /*
         *byte -> short -> int -> long -> float -> double
         */

        //switch 表达式
        int seasonCode = 2;
        String seasonName = switch (seasonCode) {
            case 0 -> "Spring";
            case 1 -> "Summer";
            case 2, 3, 4 -> "Winter";
            default -> "Unknown";
        };



    }
}
