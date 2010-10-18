package com.googlecode.yadic;

import com.googlecode.totallylazy.Callable1;

import java.util.concurrent.Callable;

public interface Container extends Resolver {
    <C> Container add(Class<C> concrete);

    <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete);

    <I, C extends I> Container addInstance(Class<I> anInterface, C instance);

    <T, A extends Callable<T>> Container addActivator(Class<T> aClass, Class<A> activator);

    <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator);

    <I, C extends I> Container decorate(Class<I> anInterface, Class<C> concrete);

    <T> Callable<T> remove(Class<T> aClass);

    <T> boolean contains(Class<T> aClass);

    <T> T get(Class<T> aClass);

    <T> Callable<T> getActivator(Class<T> aClass);
}
