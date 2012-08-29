package com.googlecode.yadic;

import com.googlecode.yadic.resolvers.MissingResolver;
import com.googlecode.yadic.resolvers.Resolvers;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Functions.returns;
import static com.googlecode.yadic.resolvers.Resolvers.*;

public class SimpleContainer extends DelegatingTypeMap implements Container {
    private SimpleContainer(TypeMap typeMap) {
        super(typeMap);
    }

    public SimpleContainer(Resolver<?> parent) {
        this(new BaseTypeMap(parent));
    }

    public SimpleContainer() {
        this(new MissingResolver());
    }

    public static SimpleContainer container(TypeMap typeMap) {
        return new SimpleContainer(typeMap);
    }

    @Override
    protected TypeMap self() {
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> aClass) {
        return (T) Resolvers.resolve(this, aClass);
    }

    public <T> Callable<T> getActivator(Class<T> aClass) {
        return asCallable(super.<T>getResolver(aClass), aClass);
    }

    public <T> Container add(final Class<T> concrete) {
        addType((Type) concrete, concrete);
        return this;
    }

    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        addType((Type) anInterface, concrete);
        return this;
    }

    public <I, C extends I> Container addInstance(Class<I> anInterface, final C instance) {
        return addActivator(anInterface, returns(instance));
    }

    @SuppressWarnings("unchecked")
    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, final Class<A> activator) {
        addType(aClass, activator(this, activator));
        return this;
    }

    public <T> Container addActivator(Class<T> aClass, final Callable<? extends T> activator) {
        addType(aClass, asResolver(activator));
        return this;
    }

    public <I, C extends I> Container decorate(final Class<I> anInterface, final Class<C> concrete) {
        addType(anInterface, decorator(this, anInterface, concrete));
        return this;
    }

    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        remove(anInterface);
        return add(anInterface, newConcrete);
    }

}