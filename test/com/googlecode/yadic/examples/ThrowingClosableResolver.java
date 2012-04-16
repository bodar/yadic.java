package com.googlecode.yadic.examples;

import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;

public class ThrowingClosableResolver<T> implements Resolver<T>, Closeable {
    private final T instance;

    public ThrowingClosableResolver(T instance) {
        this.instance = instance;
    }

    public static <T> ThrowingClosableResolver<T> forType(T instance) {
        return new ThrowingClosableResolver<T>(instance);
    }

    public T resolve(Type type) throws Exception {
        return instance;
    }

    public void close() throws IOException {
        throw new RuntimeException();
    }
}
