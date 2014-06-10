package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.callables.LazyFunction;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.yadic.generics.Types;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Callables.when;
import static com.googlecode.totallylazy.Functions.constant;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Activator<T> implements Function<Iterable<Activator<?>>, T>, Predicate<Type>, AutoCloseable, Value<Sequence<T>> {
    private final Function<? super Iterable<Activator<?>>, T> constructor;
    private final Block<? super T> destructor;
    private final Predicate<? super Type> matcher;
    private final LazyFunction<Iterable<Activator<?>>, T> instances;

    private Activator(Function<? super Iterable<Activator<?>>, T> constructor, Block<? super T> destructor, Predicate<? super Type> matcher) {
        this.matcher = matcher;
        this.constructor = constructor;
        this.destructor = destructor;
        instances = LazyFunction.lazy(constructor);
    }

    public static <T> Activator<T> activator(Function<? super Iterable<Activator<?>>, T> constructor, Block<? super T> destructor, Predicate<? super Type> matcher) {
        return new Activator<T>(constructor, destructor, matcher);
    }

    public static <T> Activator<T> activator(Class<T> aClass) {
        return activator(
                list -> Activators.create(aClass, list),
                Activators.destructor(aClass),
                type -> Types.matches(aClass, type));
    }

    @Override
    public T call(Iterable<Activator<?>> list) throws Exception {
        return instances.call(list);
    }

    @Override
    public void close() throws Exception {
        value().each((Function<? super T, Void>) destructor);
    }

    @Override
    public Sequence<T> value() {
        return instances.value();
    }

    @Override
    public boolean matches(Type other) {
        return matcher.matches(other);
    }

    @SafeVarargs
    public final Activator<T> interfaces(Class<? super T>... interfaces) {
        return types(interfaces);
    }

    public Activator<T> types(Type... types) {
        return types(sequence(types));
    }

    public Activator<T> types(Iterable<? extends Type> types) {
        return matcher(type -> sequence(types).exists(i -> Types.matches(i, type)));
    }

    public Activator<T> instance(T instance) {
        return constructor(constant(instance));
    }

    public Activator<T> constructor(Function<? super Iterable<Activator<?>>, T> constructor) {
        return activator(constructor, destructor, matcher);
    }

    public Activator<T> destructor(Block<T> destructor) {
        return activator(constructor, destructor, matcher);
    }

    public Activator<T> matcher(Predicate<? super Type> predicate) {
        return activator(constructor, destructor, predicate);
    }

    public PersistentList<Activator<?>> decorate(Type typeToDecorate, PersistentList<Activator<?>> activators) {
        return activators.map(when(a -> a.matches(typeToDecorate),
                original -> activator(
                        list -> constructor.call(Sequences.cons(original, list)),
                        destructor,
                        type -> Types.matches(typeToDecorate, type))
        ));
    }
}