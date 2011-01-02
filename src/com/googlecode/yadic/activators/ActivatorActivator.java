package com.googlecode.yadic.activators;

import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class ActivatorActivator<T> implements Callable<T> {
    private final Type activator;
    private final Resolver resolver;

    public ActivatorActivator(Type activator, Resolver resolver) {
        this.activator = activator;
        this.resolver = resolver;
    }

    @SuppressWarnings("unchecked")
    public T call() throws Exception {
        return (T) Activators.create(activator, resolver).call();
    }
}
