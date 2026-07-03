package com.uniplore;


import com.uniplore.entity.Employee;

import java.lang.reflect.*;
import java.util.Random;
import java.util.Scanner;

public class Reflection {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        /*
         * Object 类中的 getClass() 方法将会返回一个 Class 类型的实例
         */
        Employee e = new Employee("Tom", 5000, 2020, 1, 1);
        Class cl = e.getClass();
        System.out.println(e.getClass().getName() + "  " + e.getName());
        //com.uniplore.entity.Employee * * Tom   包名也作为类名的一部分

        //Class类是一个泛型类
        Class cl1 = Random.class; //if you import java.util.*;
        Class cl2 = int.class;
        Class cl3 = Double.class;

        //虚拟机为每个类型管理一个唯一的 Class 对象。 因此，可以使用==运算符比较两个类对象
        //我的理解是类似instanceof运算符
        if (e.getClass() == Employee.class) {
            System.out.println("e is an Employee");
        }

        //如果有一个 Class 类型的对象，可以用它构造类的实例。调用 getConstructor 方法将得到
        //一个 Constructor 类型的对象，然后使用 newInstance 方法来构造一个实例
        var className = "java.util.Random";
        Class cl5 = Class.forName(className);
        Object obj = cl5.getConstructor().newInstance();


    }


}


/**
 * Java 反射机制演示程序
 * 演示如何通过反射获取类的结构信息（构造器、方法、字段等）
 */
class ReflectionTesti {
    public static void main(String[] args)
            throws ClassNotFoundException {
        String name;
        // 从命令行参数或用户输入获取类名
        if (args.length > 0) {
            name = args[0];
        } else {
            var in = new Scanner(System.in);
            System.out.println("Enter class name (e.g., java.util.Date): ");
            name = in.next();
        }
        
        // 通过类名加载 Class 对象
        Class cl = Class.forName(name);
        // 获取并打印类的修饰符（如 public、final 等）
        String modifiers = Modifier.toString(cl.getModifiers());
        if (modifiers.length() > 0) {
            System.out.print(modifiers + " ");
        }
        
        // 判断是否为 sealed 类（Java 17+ 特性）
        if (cl.isSealed()) {
            System.out.print(" sealed ");
        }
        
        // 判断并打印类的类型（enum、record、interface 或 class）
        if (cl.isEnum()) {
            System.out.print("enum " + name);
        } else if (cl.isRecord()) {
            System.out.print(" record " + name);
        } else if (cl.isInterface()) {
            System.out.print("interface " + name);
        } else {
            System.out.print("class " + name);
        }
        
        // 获取并打印父类信息
        Class supercl = cl.getSuperclass();
        if (supercl != null && supercl != Object.class) {
            System.out.print(" extends " + supercl.getName());
        }


        // 打印实现的接口列表
        printInterfaces(cl);

        // 打印 permitted subclasses（sealed 类的允许子类）
        printPermittedSubclasses(cl);
        System.out.print("\n{\n");

        // 依次打印构造器、方法和字段
        printConstructors(cl);
        System.out.println();

        printMethods(cl);
        System.out.println();

        printFields(cl);
        System.out.println("}");
    }


    /**
     * 打印类的所有构造器
     * @param cl 要分析的 Class 对象
     */
    public static void printConstructors(Class cl) {
        // 获取所有声明的构造器（包括 private、protected 等）
        Constructor[] constructors = cl.getDeclaredConstructors();

        for (Constructor c : constructors) {
            String name = c.getName();
            System.out.print(" ");
            // 打印构造器的访问修饰符
            String modifiers = Modifier.toString(c.getModifiers());
            if (modifiers.length() > 0) {
                System.out.print(modifiers + " ");
            }
            System.out.print(name + "(");

            // 打印参数类型列表
            Class[] paramTypes = c.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print(paramTypes[j].getName());
            }
            System.out.println(");");
        }
    }


    /**
     * 打印类的所有方法
     * @param cl 要分析的 Class 对象
     */
    public static void printMethods(Class cl) {
        // 获取所有声明的方法（不包括继承的方法）
        Method[] methods = cl.getDeclaredMethods();
        for (Method m : methods) {
            Class retType = m.getReturnType();
            String name = m.getName();

            System.out.print(" ");
            // 打印方法修饰符、返回类型和方法名
            String modifiers = Modifier.toString(m.getModifiers());
            if (modifiers.length() > 0) {
                System.out.print(modifiers + " ");
            }
            System.out.print(retType.getName() + " " + name + "(");
            // 打印参数类型列表
            Class[] paramTypes = m.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print(paramTypes[j].getName());
            }
            System.out.println(");");
        }
    }

    /**
     * 打印类的所有字段
     * @param cl 要分析的 Class 对象
     */
    public static void printFields(Class cl) {
        // 获取所有声明的字段（包括私有字段）
        Field[] fields = cl.getDeclaredFields();
        for (Field f : fields) {
            Class type = f.getType();
            String name = f.getName();
            System.out.print("   ");
            // 打印字段修饰符（如果存在）
            String modifiers = Modifier.toString(f.getModifiers());
            if (modifiers.length() > 0) {
                System.out.print(modifiers + " ");
            }
            // 打印字段类型和名称
            System.out.println(type.getName() + " " + name + ";");
        }
    }

    /**
     * 打印 sealed 类的允许子类列表（Java 17+ 特性）
     * @param cl 要分析的 Class 对象
     */
    public static void printPermittedSubclasses(Class cl) {

        if (cl.isSealed()) {
            // 获取所有被允许继承的子类
            Class<?>[] permittedSubclasses = cl.getPermittedSubclasses();
            for (int i = 0; i < permittedSubclasses.length; i++) {
                if (i == 0) {
                    System.out.print(" permits ");
                } else {

                    System.out.print(", ");
                    System.out.print(permittedSubclasses[i].getName());
                }

            }
        }
    }

    /**
     * 打印类实现的所有接口
     * @param cl 要分析的 Class 对象
     */
    public static void printInterfaces(Class cl) {
        // 获取直接实现的接口（不包括父类实现的接口）
        Class<?>[] interfaces = cl.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (i == 0) {
                // 接口使用 extends，类使用 implements
                System.out.print(cl.isInterface() ? " extends" : " implements ");
            } else {
                System.out.print(", ");
            }
            System.out.print(interfaces[i].getName());

        }
    }
}
