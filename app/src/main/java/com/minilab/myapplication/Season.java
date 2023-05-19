package com.minilab.myapplication;

public enum Season implements xi {
    SPRING("e"), SUMMER("g"), QIU("g"), WINNER("g");

    private final String name;

    Season(String e) {
        this.name = e;
    }

    @Override
    public void print() {
        System.out.println(this.name);
    }
}
