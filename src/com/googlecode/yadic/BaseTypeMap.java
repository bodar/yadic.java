package com.googlecode.yadic;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.LazyCallable1.lazy;
import static com.googlecode.yadic.activators.Activators.create;
import static com.googlecode.yadic.generics.Types.equalTo;
import static com.googlecode.yadic.generics.Types.matches;

public class BaseTypeMap implements TypeMap {
    private final List<Pair<Type, Callable1<Type, Object>>> activators = new ArrayList<Pair<Type, Callable1<Type, Object>>>();
    protected final Resolver parent;

    public BaseTypeMap(Resolver parent) {
        this.parent = parent;
    }

    public Object resolve(Type type) {
        if (!contains(type)) {
            return parent.resolve(type);
        }
        try {
            return getActivator(type).call(type);
        } catch (ContainerException e) {
            throw e;
        } catch (Exception e) {
            throw new ContainerException(type.toString() + " cannot be created", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Callable1<Type, T> getActivator(Type type) {
        return (Callable1<Type, T>) sequence(activators).find(pairFor(type)).map(Callables.<Callable1<Type, Object>>second()).get();
    }

    public TypeMap add(Type type, Type concrete) {
        return add(type, create(concrete, this));
    }

    @SuppressWarnings("unchecked")
    public TypeMap add(Type type, Callable1<Type, ?> activator) {
        if (contains(type)) {
            throw new ContainerException(type.toString() + " already added to container");
        }
        activators.add(Pair.<Type, Callable1<Type, Object>>pair(type, (Callable1<Type, Object>) lazy(activator)));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Callable1<Type, T> remove(Type type) {
        for (int i = 0; i < activators.size(); i++) {
            Pair<Type, Callable1<Type, Object>> activator = activators.get(i);
            if(pairFor(type).matches(activator)){
                return (Callable1<Type, T>) activators.remove(i).second();
            }
        }
        throw new NoSuchElementException();
    }

    public boolean contains(Type type) {
        return sequence(activators).exists(pairFor(type));
    }

    @SuppressWarnings("unchecked")
    private LogicalPredicate<First<Type>> pairFor(Type type) {
        return where(first(Type.class), is(matches(type)));
    }
}
