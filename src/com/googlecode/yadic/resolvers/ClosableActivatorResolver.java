package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.resolvers.Resolvers.create;

public class ClosableActivatorResolver<T> extends ActivatorResolver<T> implements Closeable{
    ClosableActivatorResolver(Type activatorType, Resolver resolver) {
        super(activatorType, resolver);
    }

    public void close() throws IOException {
        if (activator instanceof Closeable) {
            ((Closeable) activator).close();
        }
    }
}
