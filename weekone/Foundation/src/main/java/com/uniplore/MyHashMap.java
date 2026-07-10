package com.uniplore;

/**
 * 自定义HashMap
 *
 * @author 杨锋
 * @date 2026/7/6
 *
 */
public class MyHashMap<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;


    @SuppressWarnings("unchecked")
    private Node<K, V>[] table = (Node<K, V>[]) new Node[DEFAULT_INITIAL_CAPACITY];
    private int size = 0;

    /**
     * Node类
     *
     */
    class Node<K, V> {
        K key;
        V value;
        Node<K, V> next = null;
    }

    /**
     * 向HashMap中插入元素
     *
     * @param key   //键
     * @param value //值
     * @return 旧值或空
     */
    public V put(K key, V value) {
        // 根据key的hash值获取对应的Node
        int hash = myHashCode(key);

        // 如果该位置为空，则直接插入
        if (table[hash] == null) {
            table[hash] = new Node<K, V>();
            table[hash].key = key;
            table[hash].value = value;
            size++;
            expand();
            return null;
        }
        // 如果该位置不为空，则遍历链表，如果存在则覆盖，不存在则插入
        Node<K, V> head = table[hash];

        while (head != null) {
            // 如果key存在，则覆盖值
            if (head.key.equals(key)) {
                V oldvalue = head.value;
                head.value = value;
                return oldvalue;
            }
            head = head.next;
        }
        // 如果链表中不存在该key，则插入新节点
        Node<K, V> newNode = new Node<K, V>();
        newNode.key = key;
        newNode.value = value;
        newNode.next = table[hash];
        table[hash] = newNode;
        size++;
        expand();
        return null;
    }

    /**
     * 根据键获取值
     *
     * @param key //键
     * @return 值或空
     */
    public V get(K key) {
        // 根据key的hash值获取对应的Node
        int hash = myHashCode(key);
        if (table[hash] == null) {
            return null;
        }
        // 遍历链表，如果存在则返回值，不存在则返回空
        Node<K, V> head = table[hash];
        while (head != null) {
            if (head.key.equals(key)) {
                return head.value;
            }
            head = head.next;
        }
        return null;
    }

    /**
     * 根据键删除元素
     *
     * @param key //键
     * @return 值或空
     */
    public V remove(K key) {
        // 根据key的hash值获取对应的Node
        int hash = myHashCode(key);
        if (table[hash] == null) {
            return null;
        }
        // 如果头节点即为要删除的节点
        Node<K, V> head = table[hash];
        if (head.key.equals(key)) {
            size--;
            table[hash] = head.next;
            return head.value;
        }
        // 遍历链表，如果存在则删除该节点，不存在则返回空
        // 当前节点
        Node<K, V> current = head.next;
        // 前驱节点
        Node<K, V> pre = head;
        while (current != null) {
            // 如果key存在，则删除该节点
            if (current.key.equals(key)) {
                size--;
                pre.next = current.next;
                return current.value;
            }
            // 否则，继续遍历
            pre = pre.next;
            current = current.next;

        }
        return null;
    }

    /**
     * 扩容
     */
    private void expand() {
        // 如果当前元素个数小于阈值，则不扩容
        if (size < table.length * DEFAULT_LOAD_FACTOR) {
            return;
        }
        // 创建新表
        Node<K, V>[] newTable = new Node[table.length * 2];
        Node<K, V>[] oldTable = table;
        // 遍历旧表，将元素重新插入新表
        table = newTable;
        size = 0;
        for (Node<K, V> node : oldTable) {
            while (node != null) {
                Node<K, V> next = node.next;
                put(node.key, node.value);
                node = next;
            }
        }

    }

    /**
     * 自定义hash函数
     *
     * @param key //键
     * @return hash值
     */
    private int myHashCode(K key) {
        // 如果key为空，则返回0
        if (key == null) {
            return 0;
        }
        // 对key的hash值进行取模，得到hash值
        return key.hashCode() & (table.length - 1);
    }

    /**
     * 获取HashMap中元素个数
     *
     * @return 元素个数
     */
    public int size() {
        return size;
    }


}
