package com.uniplore;

/**
 * 循环队列类
 *
 * @author 杨锋
 */
public class CircularQueue<E> {
    /**
     * 队头指针
     */
    private int front;

    /**
     * 队尾指针
     */
    private int rear;


    /**
     * 队列
     */
    private final E[] queue;

    public CircularQueue(int size) {
        this.front = 0;
        this.rear = 0;
        this.queue = (E[]) new Object[size];
    }

    /**
     * 判断队列是否为空
     *
     * @return 队列是否为空
     */
    public boolean isEmpty() {
        return this.front == this.rear;
    }

    /**
     * 判断队列是否已满
     *
     * @return 队列是否已满
     */
    public boolean isFull() {
        return (this.rear + 1) % this.queue.length == this.front;
    }

    /**
     * 获取队列长度
     *
     * @return 队列长度
     */
    public int getSize() {
        return (this.rear - this.front + this.queue.length) % this.queue.length;
    }

    /**
     * 入队操作
     *
     * @param e 入队元素
     */
    public void enqueue(E e) {
        if (this.isFull()) {
            throw new RuntimeException("Queue is full");
        }
        this.queue[this.rear] = e;
        this.rear = (this.rear + 1) % this.queue.length;
    }

    /**
     * 出队操作
     *
     * @return 出队元素
     */
    public E dequeue() {
        // 队列为空
        if (this.isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        // 出队元素
        E e = this.queue[this.front];
        this.queue[this.front] = null;
        this.front = (this.front + 1) % this.queue.length;
        return e;
    }
}
