package com.googlecode.yadic;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.yadic.activators.Activators.activator;
import static com.googlecode.yadic.activators.Activators.create;
import static com.googlecode.yadic.activators.Activators.decorator;

public class SimpleContainer extends BaseTypeMap implements Container {
    public SimpleContainer(Resolver missingHandler) {
        super(missingHandler);
    }

    public SimpleContainer() {
        this(new Resolver() {
            public Object resolve(Type type) {
                throw new ContainerException(type.toString() + " not found in container");
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> aClass) {
        return (T) resolve(aClass);
    }

    public <T> Callable<T> getActivator(Class<T> aClass) {
        return super.getActivator(aClass);
    }

    public <T> Container add(final Class<T> concrete) {
        return addActivator(concrete, create(concrete, this));
    }

    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        return addActivator(anInterface, create(concrete, this));
    }

    public <I, C extends I> Container addInstance(Class<I> anInterface, C instance) {
        return addActivator(anInterface, returns(instance));
    }

    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, final Class<A> activator) {
        return addActivator(aClass, activator(this, activator));
    }

    public <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator) {
        add(aClass, activator);
        return this;
    }

    public <I, C extends I> Container decorate(final Class<I> anInterface, final Class<C> concrete) {
        addActivator(anInterface, decorator(this, anInterface, concrete));
        return this;
    }

    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        remove(anInterface);
        return add(anInterface, newConcrete);
    }
}