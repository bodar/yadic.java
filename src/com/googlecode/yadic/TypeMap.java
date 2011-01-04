package com.googlecode.yadic;

import com.googlecode.totallylazy.Callable1;

import java.lang.reflect.Type;

public interface TypeMap extends Resolver {
    TypeMap add(Type type, Callable1<Type, ?> activator);

    TypeMap add(Type type, Type concrete);

    <T> Callable1<Type, T> getActivator(Type type);

    <T> Callable1<Type, T> remove(Type type);

    boolean contains(Type type);
}
