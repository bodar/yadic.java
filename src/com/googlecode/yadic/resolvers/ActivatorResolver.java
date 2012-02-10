package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.resolvers.LazyResolver.lazy;
import static com.googlecode.yadic.resolvers.Resolvers.create;

public class ActivatorResolver<T> implements Resolver<T> {
    private final Type activatorType;
    private final Resolver resolver;
    protected Object activator;

    ActivatorResolver(Type activatorType, Resolver resolver) {
        this.activatorType = activatorType;
        this.resolver = resolver;
    }

    @SuppressWarnings("unchecked")
    public T resolve(Type type) throws Exception {
        activator = create(activatorType, resolver).resolve(activatorType);
        if (activator instanceof Callable) {
            return (T) ((Callable) activator).call();
        }
        if (activator instanceof Resolver) {
            return (T) ((Resolver) activator).resolve(type);
        }
        throw new UnsupportedOperationException("Unsupported activatorType type " + activatorType);
    }
}
