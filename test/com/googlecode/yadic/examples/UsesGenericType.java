package com.googlecode.yadic.examples;

public class UsesGenericType {
    private final GenericType<Integer> value;

    public UsesGenericType(GenericType<Integer> value) {
        this.value = value;
    }

    public GenericType<Integer> getValue() {
        return value;
    }
}
