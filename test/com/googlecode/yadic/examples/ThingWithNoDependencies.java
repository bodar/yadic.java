package com.googlecode.yadic.examples;

public class ThingWithNoDependencies implements Thing {
    public Thing dependency() {
        return null;
    }
}
