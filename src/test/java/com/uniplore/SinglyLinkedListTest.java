package com.uniplore;

import org.junit.Test;

/**
 * 单向链表测试类
 *
 * @author 杨锋
 */
public class SinglyLinkedListTest {
    /**
     * 单向链表测试插入删除
     */
    @Test
    public void testSinglyLinkedList() {
        // 创建链表
        SinglyLinkedList<Integer> list = new SinglyLinkedList<>();
        System.out.println("初始化链表：" + list);

        // 插入元素
        for (int i = 1; i <= 10000; i++) {
            list.insert(i);
        }

        // 删除元素
        for (int i = 1; i <= 9000; i++) {
            list.delete(i);
        }
        System.out.println("删除1000个元素后链表：" + list);
    }
}
