package com.uniplore;

import org.junit.Test;

/**
 * @author yf
 */
public class MyHashMapTest {
    /**
     * 测试MyHashMap
     */
    @Test
    public void testMyHash() {
        // 插入200w条数据
        int count = 2_000_000;
        MyHashMap<String, String> myHash = new MyHashMap<>();
        // 插入
        for (int i = 0; i < count; i++) {
            myHash.put(String.valueOf(i), String.valueOf(i));
        }
        // 验证插入数量
        assert myHash.size() == count;

        // 随机删除一部分数据
        int removeCount = (int) (count * Math.random());
        for (int i = 0; i < removeCount; i++) {
            myHash.remove(String.valueOf(i));
        }
        // 验证删除数量
        assert myHash.size() == count - removeCount;
    }
}
