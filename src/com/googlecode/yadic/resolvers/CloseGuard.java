package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;

public class CloseGuard<T> implements Resolver<T>, Closeable{
    private final Resolver<T> resolver;
    private boolean resolved = false;

    private CloseGuard(Resolver<T> resolver) {
        this.resolver = resolver;
    }

    public static <T> CloseGuard<T> closeGuard(Resolver<T> resolver) {
        return new CloseGuard<T>(resolver);
    }

    public T resolve(Type type) throws Exception {
        resolved = true;
        return resolver.resolve(type);
    }

    public void close() throws IOException {
        if(resolved && resolver instanceof Closeable){
            ((Closeable) resolver).close();
        }
    }

}
