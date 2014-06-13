package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Block;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.resolvers.Resolvers;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Unchecked.cast;

public interface Activators {
    static <T> T create(Type genericSignature, Class<?> concrete, Iterable<? extends Activator<?>> list) throws Exception {
        return cast(Resolvers.create(concrete, ListResolver.listResolver(list)).resolve(genericSignature));
    }

    static <T> Block<T> destructor(Class<? extends T> aClass) {
        return AutoCloseable.class.isAssignableFrom(aClass) ? t -> ((AutoCloseable) t).close() : t -> { };
    }

}
