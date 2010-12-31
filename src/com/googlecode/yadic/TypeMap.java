package com.googlecode.yadic;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public interface TypeMap extends Resolver {
    <T> TypeMap add(Type type, Callable activator);

    <T> Callable<T> remove(Type type);

    <T> boolean contains(Type type);

    <T> Callable<T> getActivator(Type type);

    TypeMap add(Type type, Class<?> concrete);
}
