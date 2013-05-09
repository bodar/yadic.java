package com.googlecode.yadic;

import com.googlecode.totallylazy.Option;

import java.lang.reflect.Type;

public interface TypeMap extends Resolver<Object>, Creator {
    TypeMap addResolver(Type type, Resolver<?> resolver);

    TypeMap addResolver(Type type, Class<? extends Resolver> resolverClass);

    TypeMap addType(Type type, Type concrete);

    <T> Resolver<T> getResolver(Type type);

    <T> Resolver<T> remove(Type type);

    <T> Option<Resolver<T>> removeOption(Type type);

    boolean contains(Type type);

    TypeMap decorateType(Type anInterface, Type concrete);
}
