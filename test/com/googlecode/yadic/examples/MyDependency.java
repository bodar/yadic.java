package com.googlecode.yadic.examples;

public class MyDependency implements Thing {
    private final ThingWithNoDependencies dependency;

    public MyDependency(ThingWithNoDependencies dependency) {
        this.dependency = dependency;
    }

    public Thing dependency() {
        return dependency;
    }
}
