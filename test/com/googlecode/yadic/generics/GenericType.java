package com.googlecode.yadic.generics;

public class GenericType<T> {
    private final T instance;

    public GenericType(T instance) {
        this.instance = instance;
    }

    public T getInstance() {
        return instance;
    }
}
