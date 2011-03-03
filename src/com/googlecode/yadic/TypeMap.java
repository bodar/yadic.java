package com.googlecode.yadic;

import java.io.Closeable;
import java.lang.reflect.Type;

public interface TypeMap extends Resolver<Object>, Closeable {
    TypeMap add(Type type, Resolver<?> resolver);

    TypeMap add(Type type, Class<? extends Resolver> resolverClass);

    TypeMap add(Type type, Type concrete);

    <T> Resolver<T> getResolver(Type type);

    <T> Resolver<T> remove(Type type);

    boolean contains(Type type);

    <I, C extends I> TypeMap decorate(Type anInterface, Type concrete);
}
