package com.uniplore;

import org.junit.Test;

public class MyHashMapTest {

    @Test
    public void testMyHash() {
        int count = 2000000;
        MyHashMap<String, String> myHash = new MyHashMap<>();

        for (int i = 0; i < count; i++) {
            myHash.put(String.valueOf(i), String.valueOf(i));
        }
        assert myHash.size() == count;

        int removeCount = (int) (count * Math.random());
        for (int i = 0; i < removeCount; i++) {
            myHash.remove(String.valueOf(i));
        }
        assert myHash.size() == count - removeCount;
    }
}
