package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.generics.Types;
import com.googlecode.yadic.resolvers.Resolvers;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.yadic.collections.ListResolver.listResolver;

public interface Activators {
    static <T> Function2<Type, Iterable<Activator<?>>, T> constructor(Class<?> concrete) {
        return (type, list) -> cast(Resolvers.constructor(concrete, listResolver(list)).resolve(type));
    }

    static <T> Function2<Type, Iterable<Activator<?>>, T> staticMethod(Class<?> concrete) {
        return (type, list) -> cast(Resolvers.staticMethod(concrete, listResolver(list)).resolve(type));
    }

    static <T> Function2<Type, Iterable<Activator<?>>, T> create(Class<?> concrete) {
        return (type, list) -> cast(Resolvers.create(concrete, listResolver(list)).resolve(type));
    }

    static <T> Block<T> destructor(Class<? extends T> aClass) {
        return AutoCloseable.class.isAssignableFrom(aClass) ? t -> ((AutoCloseable) t).close() : t -> { };
    }

    static Predicate<Type> types(Type... classes) {
        return types(sequence(classes));
    }

    static Predicate<Type> types(Iterable<? extends Type> types) {
        return type -> sequence(types).exists(i -> Types.matches(i, type));
    }
}
