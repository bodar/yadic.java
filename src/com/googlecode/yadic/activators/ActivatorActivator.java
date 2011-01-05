package com.googlecode.yadic.activators;

import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

import static com.googlecode.yadic.activators.Resolvers.create;

public class ActivatorActivator<T> implements Resolver<T> {
    private final Type activator;
    private final Resolver resolver;

    public ActivatorActivator(Type activator, Resolver resolver) {
        this.activator = activator;
        this.resolver = resolver;
    }

    @SuppressWarnings("unchecked")
    public T resolve(Type type) throws Exception {
        return (T) create(activator, resolver).resolve(type);
    }
}
