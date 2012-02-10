package com.googlecode.yadic;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.yadic.resolvers.MissingResolver;
import com.googlecode.yadic.resolvers.Resolvers;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.yadic.resolvers.Resolvers.*;
import static com.googlecode.yadic.resolvers.Resolvers.asResolver;

public class SimpleContainer extends BaseTypeMap implements Container {
    public SimpleContainer(Resolver parent) {
        super(parent);
    }

    public SimpleContainer() {
        this(new MissingResolver());
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

    public <I, C extends I> Container addInstance(Class<I> anInterface, C instance) {
        return addActivator(anInterface, new IgnoresClose<I>(instance));
    }

    public <I, C extends I> Container addClosableInstance(Class<I> anInterface, C instance) {
        return addActivator(anInterface, new AlwaysClose<I>(instance));
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

    private static class IgnoresClose<I> implements Callable<I>, Closeable {
        private I instance;

        public IgnoresClose(I instance) {
            this.instance = instance;
        }

        public I call() throws Exception {
            return instance;
        }

        public void close() throws IOException {
        }
    }

    private static class AlwaysClose<I> implements Callable<I>, Closeable {
        private I instance;

        public AlwaysClose(I instance) {
            this.instance = instance;
        }

        public I call() throws Exception {
            return instance;
        }

        public void close() throws IOException {
            Closeables.close(instance);
        }
    }
}