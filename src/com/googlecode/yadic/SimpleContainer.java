package com.googlecode.yadic;

import com.googlecode.totallylazy.Callables;
import com.googlecode.yadic.activators.MissingResolver;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.curry;
import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.yadic.activators.Activators.*;

public class SimpleContainer extends BaseTypeMap implements Container {
    public SimpleContainer(Resolver parent) {
        super(parent);
    }

    public SimpleContainer() {
        this(new MissingResolver());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> aClass) {
        return (T) resolve(aClass);
    }

    public <T> Callable<T> getActivator(Class<T> aClass) {
        return curry(super.<T>getActivator(aClass), aClass);
    }

    public <T> Container add(final Class<T> concrete) {
        add(concrete, create(concrete, this));
        return this;
    }

    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        add(anInterface, create(concrete, this));
        return this;
    }

    public <I, C extends I> Container addInstance(Class<I> anInterface, C instance) {
        return addActivator(anInterface, returns(instance));
    }

    @SuppressWarnings("unchecked")
    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, final Class<A> activator) {
        add(aClass, activator(this, activator));
        return this;
    }

    public <T> Container addActivator(Class<T> aClass, final Callable<? extends T> activator) {
        add(aClass, Callables.<Type, Object>asCallable1(activator));
        return this;
    }

    public <I, C extends I> Container decorate(final Class<I> anInterface, final Class<C> concrete) {
        add(anInterface, decorator(this, anInterface, concrete));
        return this;
    }

    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        remove(anInterface);
        return add(anInterface, newConcrete);
    }

}