package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Creator;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class ActivatorResolver<T> implements Resolver<T> {
    private final Type activatorType;
    private final Creator creator;

    public ActivatorResolver(Creator creator, Type activatorType) {
        this.activatorType = activatorType;
        this.creator = creator;
    }

    @SuppressWarnings("unchecked")
    public T resolve(Type type) throws Exception {
        Object activator = creator.create(activatorType);
        if (activator instanceof Callable) {
            return (T) ((Callable) activator).call();
        }
        if (activator instanceof Resolver) {
            return (T) ((Resolver) activator).resolve(type);
        }
        throw new UnsupportedOperationException("Unsupported activatorType type " + activatorType);
    }
}
