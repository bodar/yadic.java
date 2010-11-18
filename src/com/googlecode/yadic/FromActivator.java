package com.googlecode.yadic;

import java.util.concurrent.Callable;

public class FromActivator<T> implements Callable<T> {
    private final Class<? extends T> aClass;
    private Resolver resolver;

    private FromActivator(Resolver resolver, Class<T> aClass) {
        this.resolver = resolver;
        this.aClass = aClass;
    }

    public T call() throws Exception {
        return (T) resolver.resolve(aClass);
    }

    public static <T> Callable<T> from(Resolver resolver, final Class<T> aClass) {
        return new FromActivator<T>(resolver, aClass);
    }
}