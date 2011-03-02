package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.resolvers.LazyResolver.lazy;
import static com.googlecode.yadic.resolvers.Resolvers.create;

public class ActivatorResolver<T> implements Resolver<T>, Closeable {
    private final Type activatorType;
    private Resolver<Object> activatorResolver;

    public ActivatorResolver(Type activatorType, Resolver resolver) {
        this.activatorType = activatorType;
        activatorResolver = lazy(create(activatorType, resolver));
    }

    @SuppressWarnings("unchecked")
    public T resolve(Type type) throws Exception {
        Object activator = activatorResolver.resolve(activatorType);
        if(activator instanceof Callable){
            return (T) ((Callable) activator).call();
        }
        if(activator instanceof Resolver){
            return (T) ((Resolver) activator).resolve(type);
        }
        throw new UnsupportedOperationException("Unsupported activatorType type " + activatorType);
    }

    public void close() throws IOException {
        Object activator = Resolvers.resolve(activatorResolver, activatorType);
        if(activator instanceof Closeable){
            ((Closeable) activator).close();
        }
    }
}
