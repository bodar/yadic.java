package com.googlecode.yadic;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.LazyCallable.lazy;
import static com.googlecode.yadic.CreateCallable.create;
import static com.googlecode.yadic.generics.CreateParameterizedType.createParameterizedType;
import static com.googlecode.yadic.generics.Types.equalTo;

public class SimpleContainer implements Container {
    private final List<Pair<Type, Callable>> activators = new ArrayList<Pair<Type, Callable>>();
    private final Resolver missingHandler;

    public SimpleContainer(Resolver missingHandler) {
        this.missingHandler = missingHandler;
    }

    public SimpleContainer() {
        this(new Resolver() {
            public Object resolve(Type type) {
                throw new ContainerException(type.toString() + " not found in container");
            }
        });
    }

    public Object resolve(Type type) {
        if (!contains(type)) {
            return missingHandler.resolve(type);
        }
        try {
            return getActivator(type).call();
        } catch (ContainerException e) {
            throw e;
        } catch (Exception e) {
            throw new ContainerException(type.toString() + " cannot be created", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> aClass) {
        return (T) resolve(aClass);
    }

    @SuppressWarnings("unchecked")
    public <T> Callable<T> getActivator(Type type) {
        return sequence(activators).find(pairFor(type)).map(second(Callable.class)).get();
    }

    @SuppressWarnings("unchecked")
    private LogicalPredicate<First<Type>> pairFor(Type type) {
        return where(first(Type.class), is(equalTo(type)));
    }

    public <T> Container add(final Class<T> concrete) {
        return addActivator(concrete, create(concrete, this));
    }

    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        return addActivator(anInterface, create(concrete, this));
    }

    public Container add(Type type, Class<?> concrete) {
        return addActivator(type, createParameterizedType(concrete, (ParameterizedType) type, this));
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
        return addActivator((Type) aClass, activator);
    }

    public Container addActivator(Type type, Callable activator) {
        if (contains(type)) {
            throw new ContainerException(type.toString() + " already added to container");
        }
        activators.add(pair(type, lazy(activator)));
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

    @SuppressWarnings("unchecked")
    public <T> Callable<T> remove(Type type) {
        for (int i = 0, activatorsSize = activators.size(); i < activatorsSize; i++) {
            Pair<Type, Callable> activator = activators.get(i);
            if(pairFor(type).matches(activator)){
                return activators.remove(i).second();
            }
        }
        throw new NoSuchElementException();
    }

    public <T> boolean contains(Type type) {
        return sequence(activators).exists(pairFor(type));
    }

    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        remove(anInterface);
        return add(anInterface, newConcrete);
    }
}