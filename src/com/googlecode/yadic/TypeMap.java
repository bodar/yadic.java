package com.googlecode.yadic;

import com.googlecode.totallylazy.Callable1;

import java.lang.reflect.Type;

public interface TypeMap extends Resolver<Object> {
    TypeMap add(Type type, Resolver<?> activator);

    TypeMap add(Type type, Type concrete);

    <T> Resolver<T> getResolver(Type type);

    <T> Resolver<T> remove(Type type);

    boolean contains(Type type);
}
