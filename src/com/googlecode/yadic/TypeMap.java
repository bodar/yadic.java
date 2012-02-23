package com.googlecode.yadic;

import java.io.Closeable;
import java.lang.reflect.Type;

public interface TypeMap extends Resolver<Object>, Creator {
    TypeMap addType(Type type, Resolver<?> resolver);

    TypeMap addType(Type type, Class<? extends Resolver> resolverClass);

    TypeMap addType(Type type, Type concrete);

    <T> Resolver<T> getResolver(Type type);

    <T> Resolver<T> remove(Type type);

    boolean contains(Type type);

    TypeMap decorateType(Type anInterface, Type concrete);
}
