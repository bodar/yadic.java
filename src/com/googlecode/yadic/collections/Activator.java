package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.callables.LazyFunction;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.yadic.generics.TypeFor;
import com.googlecode.yadic.generics.Types;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Callables.when;
import static com.googlecode.totallylazy.Functions.constant;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class Activator<T> implements Function2<Type, Iterable<Activator<?>>, T>, Predicate<Type>, AutoCloseable, Value<Sequence<T>> {
    private final Predicate<? super Type> matcher;
    private final Function2<? super Type, ? super Iterable<Activator<?>>, T> constructor;
    private final Block<? super T> destructor;
    private final LazyFunction<Pair<Type, Iterable<Activator<?>>>, T> instances;

    private Activator(Predicate<? super Type> matcher, Function2<? super Type, ? super Iterable<Activator<?>>, T> constructor, Block<? super T> destructor) {
        this.matcher = matcher;
        this.constructor = constructor;
        this.destructor = destructor;
        instances = LazyFunction.lazy(p -> constructor.call(p.first(), p.second()));
    }

    public static <T> Activator<T> activator(Predicate<? super Type> matcher, Function2<? super Type, ? super Iterable<Activator<?>>, T> constructor, Block<? super T> destructor) {
        return new Activator<T>(matcher, constructor, destructor);
    }

    public static <T> Activator<T> concreate(Class<T> aClass) {
        return activator(
                Activators.types(aClass),
                Activators.create(aClass),
                Activators.destructor(aClass)
        );
    }

    public static <T> Activator<T> instance(T instance) {
        return Activator.<T>concreate(cast(instance.getClass())).
                constructor(constant(instance));
    }

    @Override
    public T call(Type type, Iterable<Activator<?>> list) throws Exception {
        return instances.call(Pair.pair(type, list));
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

    @SafeVarargs
    public final Activator<T> types(TypeFor<? extends T>... types) {
        return types(sequence(types).map(TypeFor::get));
    }

    public Activator<T> types(Type... types) {
        return types(sequence(types));
    }

    public Activator<T> types(Iterable<? extends Type> types) {
        return matcher(Activators.types(types));
    }

    public Activator<T> constructor(Returns<T> constructor) {
        return constructor((type, list) -> constructor.call());
    }

    public Activator<T> constructor(Function<? super Iterable<Activator<?>>, T> constructor) {
        return constructor((type, list) -> constructor.call(list));
    }

    public Activator<T> constructor(Function2<Type, ? super Iterable<Activator<?>>, T> constructor) {
        return activator(matcher, constructor, destructor);
    }

    public Activator<T> destructor(Block<T> destructor) {
        return activator(matcher, constructor, destructor);
    }

    public Activator<T> matcher(Predicate<? super Type> predicate) {
        return activator(predicate, constructor, destructor);
    }

    public PersistentList<Activator<?>> decorate(Type typeToDecorate, PersistentList<Activator<?>> activators) {
        return activators.map(when(a -> a.matches(typeToDecorate),
                original -> Activator.activator(
                        type -> Types.matches(typeToDecorate, type),
                        (type, list) -> constructor.call(type, Sequences.cons(original, list)),
                        destructor
                )
        ));
    }
}