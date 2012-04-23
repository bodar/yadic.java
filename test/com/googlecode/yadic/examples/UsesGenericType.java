package com.googlecode.yadic.examples;

public class UsesGenericType {
    private final Instance<Integer> value;

    public UsesGenericType(Instance<Integer> value) {
        this.value = value;
    }

    public UsesGenericType(GenericType<Integer> value) {
        this.value = value;
    }

    public Instance<Integer> instance() {
        return value;
    }
}
