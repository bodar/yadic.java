package com.googlecode.yadic;

import java.util.concurrent.Callable;

public abstract class DelegatingContainer extends DelegatingTypeMap implements Container{
    private final Container container;

    public DelegatingContainer(Container container) {
        super(container);
        this.container = container;
    }

    public <C> Container add(Class<C> concrete) {
        return container.add(concrete);
    }

    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        return container.add(anInterface, concrete);
    }

    public <I, C extends I> Container addInstance(Class<I> anInterface, C instance) {
        return container.addInstance(anInterface, instance);
    }

    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, Class<A> activator) {
        return container.addActivator(aClass, activator);
    }

    public <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator) {
        return container.addActivator(aClass, activator);
    }

    public <I, C extends I> Container decorate(Class<I> anInterface, Class<C> concrete) {
        return container.decorate(anInterface, concrete);
    }

    public <T> T get(Class<T> aClass) {
        return container.get(aClass);
    }

    public <T> Callable<T> getActivator(Class<T> aClass) {
        return container.getActivator(aClass);
    }

    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        return container.replace(anInterface, newConcrete);
    }
}
