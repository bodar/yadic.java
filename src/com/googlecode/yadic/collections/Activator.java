package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.callables.LazyFunction;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.yadic.generics.Types;
import com.googlecode.yadic.resolvers.Resolvers;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Functions.constant;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.yadic.resolvers.Resolvers.create;

public class Activator<T> implements Function<Iterable<Activator<?>>, T>, Predicate<Type>, AutoCloseable {
    private final Function<? super Iterable<Activator<?>>, T> constructor;
    private final Block<? super T> destructor;
    private final Predicate<? super Type> matcher;
    private final LazyFunction<Iterable<Activator<?>>, T> instances;

    public Activator(Function<? super Iterable<Activator<?>>, T> constructor, Block<? super T> destructor, Predicate<? super Type> matcher) {
        this.matcher = matcher;
        this.constructor = constructor;
        this.destructor = destructor;
        instances = LazyFunction.lazy(constructor);
    }

    @Override
    public T call(Iterable<Activator<?>> list) throws Exception {
        return instances.call(list);
    }

    @Override
    public void close() throws Exception {
        instances.value().each((Function<? super T, Void>) destructor);
    }

    @Override
    public boolean matches(Type other) {
        return matcher.matches(other);
    }

    public static <T> Activator<T> activator(Class<T> aClass) {
        return new Activator<T>(
                list -> Activators.create(aClass, list),
                Activators.destructor(aClass),
                type -> Types.matches(aClass, type));
    }

    @SafeVarargs
    public final Activator<T> interfaces(Class<? super T>... interfaces) {
        return new Activator<>(
                constructor,
                destructor,
                type -> sequence(interfaces).exists(i -> Types.matches(i, type)));
    }


    public Activator<T> constructor(Function<? super Iterable<Activator<?>>, T> constructor) {
        return new Activator<>(constructor, destructor, matcher);
    }

    public Activator<T> instance(T instance) {
        return constructor(constant(instance));
    }

    public Activator<T> destructor(Block<T> destructor) {
        return new Activator<>(constructor, destructor, matcher);
    }

    public PersistentList<Activator<?>> decorate(Type typeToDecorate, PersistentList<Activator<?>> activators) {
        Activator<?> original = activators.find(a -> a.matches(typeToDecorate)).get();
        return activators.delete(original).
                cons(new Activator<>(
                        list -> constructor.call(Sequences.cons(original, list)),
                        destructor,
                        type -> Types.matches(typeToDecorate, type)));
    }

}
