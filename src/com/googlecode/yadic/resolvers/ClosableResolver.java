package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Runnable1;
import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Sequences.sequence;

public class ClosableResolver<T> implements Resolver<T>, Closeable{
    private final Resolver<T> resolver;
    private final List<Closeable> closeables = new ArrayList<Closeable>();

    public ClosableResolver(Resolver<T> resolver) {
        this.resolver = resolver;
    }

    public void close() throws IOException {
        sequence(closeables).forEach(Resolvers.close());
    }

    public T resolve(Type type) throws Exception {
        T instance = resolver.resolve(type);
        if(instance instanceof Closeable){
            closeables.add((Closeable) instance);
        }
        return instance;
    }
}
