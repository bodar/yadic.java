package com.googlecode.yadic.examples;

public class MyStaticMethodClass {
    public final String constructedBy;

    public MyStaticMethodClass(String constructedBy) {
        this.constructedBy = constructedBy;
    }

    public static MyStaticMethodClass myStaticMethodClass1(String parameter) {
        return new MyStaticMethodClass("myStaticMethodClass1");
    }

    public static MyStaticMethodClass myStaticMethodClass2a(String parameter1, Boolean parameter2) {
        return new MyStaticMethodClass("myStaticMethodClass2a");
    }

    public static MyStaticMethodClass myStaticMethodClass2(String parameter1, Integer parameter2) {
        return new MyStaticMethodClass("myStaticMethodClass2");
    }
}
