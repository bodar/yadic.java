package com.googlecode.yadic.examples;

public class MyThing implements Thing {
    private final MyDependency dependency;

    public MyThing(MyDependency dependency) {
        this.dependency = dependency;
    }

    public Thing dependency() {
        return dependency;
    }
}
