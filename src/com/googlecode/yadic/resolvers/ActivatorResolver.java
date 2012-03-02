package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class ActivatorResolver<T> implements Resolver<T> {
    private final Type activatorType;
    private final TypeMap container;

    public ActivatorResolver(TypeMap container, Type activatorType) {
        this.activatorType = activatorType;
        this.container = container;
    }

    @SuppressWarnings("unchecked")
    public T resolve(Type type) throws Exception {
        if(!container.contains(activatorType)){
            container.addType(activatorType, activatorType);
        }
        Object activator = container.resolve(activatorType);
        if (activator instanceof Callable) {
            return (T) ((Callable) activator).call();
        }
        if (activator instanceof Resolver) {
            return (T) ((Resolver) activator).resolve(type);
        }
        throw new UnsupportedOperationException("Unsupported activatorType type " + activatorType);
    }
}
