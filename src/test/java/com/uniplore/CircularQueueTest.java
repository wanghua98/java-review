package com.uniplore;

import org.junit.Test;
/**
 * 循环队列测试类
 *
 * @author 杨锋
 */
public class CircularQueueTest {

    /**
     * 测试循环队列
     */
    @Test
    public void testCircularQueue() {
        CircularQueue<Integer> circularQueue = new CircularQueue<Integer>(5);
        circularQueue.enqueue(1);
        circularQueue.enqueue(2);
        circularQueue.enqueue(3);
        circularQueue.enqueue(4);
        //circularQueue.enqueue(5);
        System.out.println(circularQueue.dequeue());
        System.out.println(circularQueue.dequeue());
        circularQueue.enqueue(6);
        circularQueue.enqueue(7);
        System.out.println(circularQueue.dequeue());
        System.out.println(circularQueue.dequeue());
        System.out.println(circularQueue.dequeue());
        System.out.println(circularQueue.dequeue());
        assert circularQueue.isEmpty();
    }
}
