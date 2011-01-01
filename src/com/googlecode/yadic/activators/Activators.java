package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callers;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class Activators {
    public static <I, C> Callable<C> decorator(final TypeMap typeMap, final Class<I> anInterface, final Class<C> concrete) {
        final Callable<I> existing = typeMap.remove(anInterface);
        return create(concrete, new Resolver() {
            public Object resolve(Type type) {
                return type.equals(anInterface) ? Callers.call(existing) : typeMap.resolve(type);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T, A extends Callable<T>> Callable<T> activator(final Resolver resolver, final Class<A> activator) {
        return new Callable<T>() {
            public T call() throws Exception {
                return (T) create(activator, resolver).call();
            }
        };
    }

    public static <T> Callable<T> create(final Class<T> concrete, final Resolver resolver) {
        return new ConstructorActivator<T>(resolver, concrete, concrete);
    }

    public static <T> Callable<T> create(final Type type, Class<T> concrete, final Resolver resolver) {
        return new ConstructorActivator<T>(resolver, type, concrete);
    }

}
