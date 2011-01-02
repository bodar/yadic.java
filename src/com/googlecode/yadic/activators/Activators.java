package com.googlecode.yadic.activators;

import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class Activators {
    public static <I, C> Callable<C> decorator(final TypeMap typeMap, final Class<I> anInterface, final Class<C> concrete) {
        return create(concrete, new DecoratorResolver(anInterface, typeMap.remove(anInterface), typeMap));
    }

    public static Callable decorator(final TypeMap typeMap, final Type anInterface, final Type concrete) {
        return create(concrete, new DecoratorResolver(anInterface, typeMap.remove(anInterface), typeMap));
    }

    @SuppressWarnings("unchecked")
    public static <T, A extends Callable<T>> Callable<T> activator(final Resolver resolver, final Class<A> activator) {
        return new ActivatorActivator<T>(activator, resolver);
    }

    public static Callable activator(final Resolver resolver, final Type activator) {
        return new ActivatorActivator(activator, resolver);
    }

    public static <T> Callable<T> create(final Class<T> concrete, final Resolver resolver) {
        return new ConstructorActivator<T>(resolver, concrete);
    }

    public static Callable create(final Type concrete, final Resolver resolver) {
        return new ConstructorActivator(resolver, concrete);
    }
}
