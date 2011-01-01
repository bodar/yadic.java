package com.googlecode.yadic.examples;

public class GenericType<T> {
    private final T instance;

    public GenericType(T instance) {
        this.instance = instance;
    }

    public T getInstance() {
        return instance;
    }
}
