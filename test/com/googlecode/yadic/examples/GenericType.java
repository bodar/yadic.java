package com.googlecode.yadic.examples;

public class GenericType<T> implements Instance<T>{
    private final T instance;

    public GenericType(T instance) {
        this.instance = instance;
    }

    public T instance() {
        return instance;
    }
}
