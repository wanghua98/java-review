package com.uniplore.designpatterns.singleton;

/**
 * 单例模式
 *
 * @author 杨锋
 */
public class Singleton {
    /**
     * 单例对象
     */
    private static Singleton instance;

    /**
     * 私有构造函数，防止被实例化
     */
    private Singleton() {
    }

    /**
     * 获取唯一实例线程不安全
     *
     * @return 单例对象
     */
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    /**
     * 获取唯一实例线程安全
     *
     * @return 单例对象
     */
    public static synchronized Singleton getInstanceThreadSafe() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    /**
     * 懒汉式变种
     *
     * @return 单例对象
     */
    public static Singleton getInstanceLazy() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
