package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Sequences.sequence;

public class ClosableResolver<T> implements Resolver<T>, Closeable{
    private final Resolver<? extends T> resolver;
    private final List<Closeable> closeables = new ArrayList<Closeable>();

    private ClosableResolver(Resolver<? extends T> resolver) {
        this.resolver = resolver;
    }

    public static <T> ClosableResolver<T> closable(Resolver<? extends T> resolver) {
        return new ClosableResolver<T>(resolver);
    }

    public void close() throws IOException {
        sequence(closeables).forEach(Closeables.close());
    }

    public T resolve(Type type) throws Exception {
        T instance = resolver.resolve(type);
        if(instance instanceof Closeable){
            closeables.add((Closeable) instance);
        }
        return instance;
    }
}
