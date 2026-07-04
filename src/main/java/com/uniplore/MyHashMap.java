package com.uniplore;

public class MyHashMap<k, v> {

    @SuppressWarnings("unchecked")
    private Node<k, v>[] table = (Node<k, v>[]) new Node[16];
    private int size = 0;

    class Node<K, V> {
        K key;
        V value;
        Node<K, V> next = null;
    }

    public v put(k key, v value) {
        int hash = myHashCode(key);

        if (table[hash] == null) {
            table[hash] = new Node<k, v>();
            table[hash].key = key;
            table[hash].value = value;
            size++;
            expand();
            return null;
        }
        Node<k, v> head = table[hash];

        while (head != null) {
            if (head.key.equals(key)) {
                v oldvalue = head.value;
                head.value = value;
                return oldvalue;
            }
            head = head.next;
        }

        Node<k, v> newNode = new Node<k, v>();
        newNode.key = key;
        newNode.value = value;
        newNode.next = table[hash];
        table[hash] = newNode;
        size++;
        expand();
        return null;
    }

    public v get(k key) {
        int hash = myHashCode(key);
        if (table[hash] == null) {
            return null;
        }
        Node<k, v> head = table[hash];
        while (head != null) {
            if (head.key.equals(key)) {
                return head.value;
            }
            head = head.next;
        }
        return null;
    }

    public v remove(k key) {
        int hash = myHashCode(key);
        if (table[hash] == null) {
            return null;
        }
        Node<k, v> head = table[hash];
        if (head.key.equals(key)) {
            size--;
            table[hash] = head.next;
            return head.value;
        }
        Node<k, v> current = head.next;
        Node<k, v> pre = head;
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

    //必要时扩容
    private void expand() {
        if (size < table.length * 0.75) {
            return;
        }
        Node<k, v>[] newTable = new Node[table.length * 2];
        Node<k, v>[] oldTable = table;
        table = newTable;
        size = 0;
        for (Node<k, v> node : oldTable) {
            while (node != null) {
                Node<k, v> next = node.next;
                put(node.key, node.value);
                node = next;
            }
        }

    }

    private int myHashCode(k key) {
        if (key == null) {
            return 0;
        }
        return key.hashCode() & (table.length - 1);
    }

    public int size() {
        return size;
    }


}
