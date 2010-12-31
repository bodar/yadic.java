package com.googlecode.yadic;

import com.googlecode.totallylazy.Callable1;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public interface Container extends Resolver {
    <C> Container add(Class<C> concrete);

    <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete);
    Container add(Type type, Class<?> concrete);

    <I, C extends I> Container addInstance(Class<I> anInterface, C instance);

    <T, A extends Callable<T>> Container addActivator(Class<T> aClass, Class<A> activator);

    <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator);

    <T> Container addActivator(Type type, Callable<? extends T> activator);

    <I, C extends I> Container decorate(Class<I> anInterface, Class<C> concrete);

    <T> Callable<T> remove(Type type);

    <T> boolean contains(Type type);

    <T> T get(Class<T> aClass);

    <T> Callable<T> getActivator(Type type);

    <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete);
}
