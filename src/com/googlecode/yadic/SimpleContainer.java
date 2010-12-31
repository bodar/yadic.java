package com.googlecode.yadic;

import com.googlecode.totallylazy.Callers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.LazyCallable.lazy;
import static com.googlecode.yadic.CreateCallable.create;
import static com.googlecode.yadic.generics.CreateParameterizedType.createParameterizedType;
import static com.googlecode.yadic.generics.Types.equalTo;

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
        return add(activator).addActivator(aClass, new Callable<T>() {
            public T call() throws Exception {
                return get(activator).call();
            }
        });
    }

    public <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator) {
        add(aClass, activator);
        return this;
    }

    public <I, C extends I> Container decorate(final Class<I> anInterface, final Class<C> concrete) {
        final Callable<?> existing = remove(anInterface);
        addActivator(anInterface, lazy(create(concrete, new Resolver() {
            public Object resolve(Type type) {
                return type.equals(anInterface) ? Callers.call(existing) : SimpleContainer.this.resolve(type);
            }
        })));
        return this;
    }

    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        remove(anInterface);
        return add(anInterface, newConcrete);
    }
}