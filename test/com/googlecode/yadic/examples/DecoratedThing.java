package com.googlecode.yadic.examples;

public class DecoratedThing implements Thing {
    private final Thing dependency;

    public DecoratedThing(Thing dependency) {
        this.dependency = dependency;
    }

    public Thing dependency() {
        return dependency;
    }
}
