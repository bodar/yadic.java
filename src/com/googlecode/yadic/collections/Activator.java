package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.callables.LazyFunction;
import com.googlecode.yadic.generics.Types;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.yadic.resolvers.Resolvers.create;

public class Activator<T> implements Function<Iterable<Activator<?>>, T>, Predicate<Type>, AutoCloseable {
    private final LazyFunction<Iterable<Activator<?>>, T> constructor;
    private final Block<? super T> destructor;
    private final Predicate<? super Type> matcher;

    private Activator(LazyFunction<Iterable<Activator<?>>, T> constructor, Block<? super T> destructor, Predicate<? super Type> matcher) {
        this.matcher = matcher;
        this.constructor = constructor;
        this.destructor = destructor;
    }

    public static <T> Activator<T> activator(Class<T> aClass) {
        return new Activator<T>(
                LazyFunction.lazy(list -> cast(create(aClass, ListResolver.listResolver(list)).resolve(aClass))),
                AutoCloseable.class.isAssignableFrom(aClass) ? t -> ((AutoCloseable) t).close() : t -> {},
                type -> Types.matches(aClass, type));
    }

    @Override
    public T call(Iterable<Activator<?>> list) throws Exception {
        return constructor.call(list);
    }

    @Override
    public void close() throws Exception {
        constructor.value().each((Function<? super T, Void>) destructor);
    }

    @Override
    public boolean matches(Type other) {
        return matcher.matches(other);
    }

    @SafeVarargs
    public final Activator<T> interfaces(Class<? super T>... interfaces) {
        return new Activator<>(
                constructor,
                destructor,
                type -> sequence(interfaces).exists(i -> Types.matches(i, type)));
    }
}
