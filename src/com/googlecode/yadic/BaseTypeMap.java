package com.googlecode.yadic;

import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.yadic.activators.ConstructorActivator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.LazyCallable.lazy;
import static com.googlecode.yadic.activators.Activators.create;
import static com.googlecode.yadic.generics.Types.equalTo;

public class BaseTypeMap implements TypeMap {
    private final List<Pair<Type, Callable>> activators = new ArrayList<Pair<Type, Callable>>();
    protected final Resolver missingHandler;

    public BaseTypeMap(Resolver missingHandler) {
        this.missingHandler = missingHandler;
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
    public Callable getActivator(Type type) {
        return sequence(activators).find(pairFor(type)).map(second(Callable.class)).get();
    }

    public TypeMap add(Type type, Class<?> concrete) {
        return add(type, create(type, concrete, this));
    }

    public <T> TypeMap add(Type type, Callable activator) {
        if (contains(type)) {
            throw new ContainerException(type.toString() + " already added to container");
        }
        activators.add(pair(type, lazy(activator)));
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

    @SuppressWarnings("unchecked")
    private LogicalPredicate<First<Type>> pairFor(Type type) {
        return where(first(Type.class), is(equalTo(type)));
    }
}
