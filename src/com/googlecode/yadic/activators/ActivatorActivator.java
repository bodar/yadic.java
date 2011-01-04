package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

public class ActivatorActivator<T> implements Callable1<Type, T> {
    private final Type activator;
    private final Resolver resolver;

    public ActivatorActivator(Type activator, Resolver resolver) {
        this.activator = activator;
        this.resolver = resolver;
    }

    @SuppressWarnings("unchecked")
    public T call(Type type) throws Exception {
        return (T) Activators.create(activator, resolver).call(type);
    }
}
