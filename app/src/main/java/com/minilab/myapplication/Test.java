package com.minilab.myapplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Test {
    public static void main(String[] args) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            oos.writeLong(Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = bos.toByteArray();

// 反序列化 - 错误的方式
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        long result = 0l;
        try {
            ois = new ObjectInputStream(bis);

            result = ois.readLong();
            System.out.print(result); // 输出 -1
        } catch (Exception e) {
            System.out.println(result);
        }
    }
}
