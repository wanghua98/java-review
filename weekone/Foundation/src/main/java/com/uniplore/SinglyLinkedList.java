package com.uniplore;

/**
 * 单向链表类
 *
 * @param <T> 节点数据类型
 * @author 杨锋
 */
public class SinglyLinkedList<T> {

    /**
     * 链表头节点
     */
    private Node<T> head = new Node<>();

    /**
     * 链表头节点
     *
     * @param <T> 节点数据类型
     */
    private static class Node<T> {
        T data;
        Node<T> next;
    }

    /**
     * 插入节点
     *
     * @param data 节点数据
     */
    public void insert(T data) {
        // 创建新节点
        Node<T> newNode = new Node<>();
        newNode.data = data;
        newNode.next = null;

        Node<T> current = this.head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
    }

    /**
     * 打印链表重写toString方法
     */
    @Override
    public String toString() {
        // 遍历链表
        Node<T> current = head.next;
        // 遍历链表，将节点数据添加到字符串构建器中
        StringBuilder sb = new StringBuilder();
        // 添加链表开始符号
        sb.append("[");
        while (current != null) {
            // 添加节点数据和逗号空格
            sb.append(current.data).append(", ");
            // 移动到下一个节点
            current = current.next;
        }
        // 移除最后一个逗号和空格
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        // 添加链表结束符号
        sb.append("]");
        return sb.toString();
    }

    /**
     * 删除节点
     *
     * @param data 节点数据
     */
    public void delete(T data) {
        // 遍历链表
        // 当前节点
        Node<T> current = this.head.next;
        // 上一个节点
        Node<T> previous = null;
        while (current != null) {
            // 如果当前节点数据等于要删除的数据
            if (current.data.equals(data)) {
                if (previous == null) {
                    // 如果上一个节点为空，则将头节点指向当前节点的下一个节点
                    this.head = current.next;
                } else {
                    // 否则，将上一个节点的下一个节点指向当前节点的下一个节点
                    previous.next = current.next;
                }
                return;
            }
            // 移动上一个节点和当前节点到下一个节点
            previous = current;
            current = current.next;

        }
    }

}
