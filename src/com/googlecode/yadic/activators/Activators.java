package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;

public class Activators {
    public static <I, C> Callable1<Type, C> decorator(final TypeMap typeMap, final Class<I> anInterface, final Class<C> concrete) {
        return create(concrete, new DecoratorResolver(anInterface, typeMap.remove(anInterface), typeMap));
    }

    public static Callable1 decorator(final TypeMap typeMap, final Type anInterface, final Type concrete) {
        return create(concrete, new DecoratorResolver(anInterface, typeMap.remove(anInterface), typeMap));
    }

    public static <T, A extends Callable1<Type, T>> Callable1<Type, T> activator(final Resolver resolver, final Class<A> activator) {
        return new ActivatorActivator<T>(activator, resolver);
    }

    public static Callable1 activator(final Resolver resolver, final Type activator) {
        return new ActivatorActivator(activator, resolver);
    }

    public static <T> Callable1<Type, T> create(final Class<T> concrete, final Resolver resolver) {
        return new ConstructorActivator<T>(resolver, concrete);
    }

    public static Callable1<Type, Object> create(final Type concrete, final Resolver resolver) {
        return new ConstructorActivator<Object>(resolver, concrete);
    }
}
