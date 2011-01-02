package com.googlecode.yadic;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public interface TypeMap extends Resolver {
    TypeMap add(Type type, Callable activator);

    TypeMap add(Type type, Type concrete);

    <T> Callable<T> getActivator(Type type);

    <T> Callable<T> remove(Type type);

    boolean contains(Type type);
}
