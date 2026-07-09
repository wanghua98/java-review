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

    /*
     * Node
     * @description 定义Node类
     */
    class Node<K, V> {
        K key;
        V value;
        Node<K, V> next = null;
    }

    /*
     * put
     * @description 向HashMap中插入元素
     * @param key //键
     * @param value //值
     * @return 旧值或空
     */
    public V put(K key, V value) {
        int hash = myHashCode(key);

        if (table[hash] == null) {
            table[hash] = new Node<K, V>();
            table[hash].key = key;
            table[hash].value = value;
            size++;
            expand();
            return null;
        }
        Node<K, V> head = table[hash];

        while (head != null) {
            if (head.key.equals(key)) {
                V oldvalue = head.value;
                head.value = value;
                return oldvalue;
            }
            head = head.next;
        }

        Node<K, V> newNode = new Node<K, V>();
        newNode.key = key;
        newNode.value = value;
        newNode.next = table[hash];
        table[hash] = newNode;
        size++;
        expand();
        return null;
    }

    /*
     * get
     * @description 根据键获取值
     * @param key //键
     * @return 值或空
     */
    public V get(K key) {
        // 根据key的hash值获取对应的Node
        int hash = myHashCode(key);
        if (table[hash] == null) {
            return null;
        }
        Node<K, V> head = table[hash];
        while (head != null) {
            if (head.key.equals(key)) {
                return head.value;
            }
            head = head.next;
        }
        return null;
    }

    /*
     * remove
     * @description 根据键删除元素
     * @param key //键
     * @return 值或空
     */
    public V remove(K key) {
        int hash = myHashCode(key);
        if (table[hash] == null) {
            return null;
        }
        Node<K, V> head = table[hash];
        if (head.key.equals(key)) {
            size--;
            table[hash] = head.next;
            return head.value;
        }
        Node<K, V> current = head.next;
        Node<K, V> pre = head;
        while (current != null) {
            if (current.key.equals(key)) {
                size--;
                pre.next = current.next;
                return current.value;
            }
            pre = pre.next;
            current = current.next;

        }
        return null;
    }

    /*
     * expand
     * @description 扩容
     */
    //必要时扩容
    private void expand() {
        if (size < table.length * DEFAULT_LOAD_FACTOR) {
            return;
        }
        Node<K, V>[] newTable = new Node[table.length * 2];
        Node<K, V>[] oldTable = table;
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

    /*
     * myHashCode
     * @description 自定义hash函数
     * @param key //键
     * @return hash值
     */
    private int myHashCode(K key) {
        if (key == null) {
            return 0;
        }
        return key.hashCode() & (table.length - 1);
    }

    /*
     * size
     * @description 获取HashMap中元素个数
     * @return 元素个数
     */
    public int size() {
        return size;
    }


}
