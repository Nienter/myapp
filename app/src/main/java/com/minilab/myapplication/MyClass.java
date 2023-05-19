package com.minilab.myapplication;

import java.io.Serializable;

public class MyClass implements Serializable {
    private static final long serialVersionUID = 3L;

    public MyClass(long a) {
        this.a = a;
    }

    public MyClass() {

    }

    private long a;

    public long getA() {
        return a;
    }

    public void setA(long a) {
        this.a = a;
    }
// 构造方法、getter/setter 略
}