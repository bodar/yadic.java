package com.googlecode.yadic.examples;

public class DecoratedGenericType<T> implements Instance<T>{
    private final Instance<T> decorated;

    public DecoratedGenericType(Instance<T> decorated) {
        this.decorated = decorated;
    }

    public T instance() {
        return decorated.instance();
    }
}
