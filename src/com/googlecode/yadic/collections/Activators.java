package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Block;
import com.googlecode.yadic.resolvers.Resolvers;

import static com.googlecode.totallylazy.Unchecked.cast;

public interface Activators {
    static <T> T create(Class<?> aClass, Iterable<? extends Activator<?>> list) throws Exception {
        return cast(Resolvers.create(aClass, ListResolver.listResolver(list)).resolve(aClass));
    }

    static <T> Block<T> destructor(Class<? extends T> aClass) {
        return AutoCloseable.class.isAssignableFrom(aClass) ? t -> ((AutoCloseable) t).close() : t -> {
        };
    }

}
