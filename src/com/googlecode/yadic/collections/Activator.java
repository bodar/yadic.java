package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.callables.LazyFunction;
import com.googlecode.yadic.generics.Types;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.yadic.resolvers.Resolvers.create;

public class Activator<T> implements Function<Iterable<Activator<?>>, T>, Predicate<Type>, AutoCloseable {
    private final LazyFunction<Iterable<Activator<?>>, T> constructor;
    private final Block<? super T> destructor;
    private final Predicate<Type> matcher;

    private Activator(Function<? super Iterable<? extends Activator<?>>, ?> constructor, Block<? super T> destructor, Predicate<Type> matcher) {
        this.matcher = matcher;
        this.constructor = LazyFunction.lazy(list -> cast(constructor.apply(list)));
        this.destructor = destructor;
    }

    public static <T> Activator<T> activator(Class<T> aClass) {
        return new Activator<>(
                list -> create(aClass, ListResolver.listResolver(list)).resolve(aClass),
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
}
