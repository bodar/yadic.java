package com.googlecode.yadic;

import com.googlecode.totallylazy.Mapper;

import java.util.concurrent.Callable;

public interface Container extends TypeMap {
    <C> Container add(Class<C> concrete);

    <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete);

    <I, C extends I> Container addInstance(Class<I> anInterface, C instance);

    <T, A extends Callable<T>> Container addActivator(Class<T> aClass, Class<A> activator);

    <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator);

    <I, C extends I> Container decorate(Class<I> anInterface, Class<C> concrete);

    <T> T get(Class<T> aClass);

    <T> Callable<T> getActivator(Class<T> aClass);

    <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete);

    class functions{
        public static <T> Mapper<Container, T> get(final Class<T> aClass) {
            return new Mapper<Container, T>() {
                @Override
                public T call(Container container) throws Exception {
                    return container.get(aClass);
                }
            };
        }
    }
}