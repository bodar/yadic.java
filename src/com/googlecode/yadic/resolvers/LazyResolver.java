package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.synchronizedMap;

public class LazyResolver<T> implements Resolver<T> {
    private final Resolver<? extends T> resolver;
    private Map<Type, T> state = synchronizedMap(new HashMap<Type, T>());

    private LazyResolver(Resolver<? extends T> resolver) {
        this.resolver = resolver;
    }

    public static <T> LazyResolver<T> lazy(Resolver<? extends T> resolver) {
        return new LazyResolver<T>(resolver);
    }

    public final T resolve(Type type) throws Exception {
        synchronized (state) {
            if (!state.containsKey(type)) {
                state.put(type, resolver.resolve(type));
            }
            return state.get(type);
        }
    }
}

