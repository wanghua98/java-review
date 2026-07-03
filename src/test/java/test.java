import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class test {

    /**
     * 测试线程，利用线程池进行异步操作
     * @param args
     */

    private static final ExecutorService e = Executors.newFixedThreadPool(5);
    public static void main(String[] args) {

        Runnable r = () ->{
            for (int i = 0; i < 100; i++) {
                System.out.println(Thread.currentThread().getName() + " " + i);
            }
        };

        e.submit(r);
        System.out.println("主线程已经完成");

    }
}
