package com.googlecode.yadic.examples;

public class DependsOnMyThing implements Thing {
    private final MyThing dependency;

    public DependsOnMyThing(MyThing dependency) {
        this.dependency = dependency;
    }

    public Thing dependency() {
        return dependency;
    }
}
