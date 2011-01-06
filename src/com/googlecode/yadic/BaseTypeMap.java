package com.googlecode.yadic;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.yadic.activators.Resolvers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.LazyCallable1.lazy;
import static com.googlecode.yadic.activators.Resolvers.activator;
import static com.googlecode.yadic.activators.Resolvers.asCallable1;
import static com.googlecode.yadic.activators.Resolvers.asResolver;
import static com.googlecode.yadic.activators.Resolvers.create;
import static com.googlecode.yadic.generics.Types.matches;

public class BaseTypeMap implements TypeMap {
    private final List<Pair<Type, Resolver<Object>>> activators = new ArrayList<Pair<Type, Resolver<Object>>>();
    protected final Resolver parent;

    public BaseTypeMap(Resolver parent) {
        this.parent = parent;
    }

    public Object resolve(Type type) throws Exception {
        if (!contains(type)) {
            return parent.resolve(type);
        }
        return Resolvers.resolve(getResolver(type), type);
    }

    @SuppressWarnings("unchecked")
    public <T> Resolver<T> getResolver(Type type) {
        return (Resolver<T>) sequence(activators).find(pairFor(type)).map(Callables.<Resolver<Object>>second()).get();
    }

    public TypeMap add(Type type, Type concrete) {
        return add(type, create(concrete, this));
    }

    public TypeMap add(Type type, Class<? extends Resolver> resolverClass) {
        return add(type, activator(this, resolverClass));
    }

    @SuppressWarnings("unchecked")
    public TypeMap add(Type type, Resolver<?> resolver) {
        if (contains(type)) {
            throw new ContainerException(type.toString() + " already added to container");
        }
        activators.add(Pair.<Type, Resolver<Object>>pair(type, asResolver(lazy(asCallable1(resolver)))));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Resolver<T> remove(Type type) {
        for (int i = 0; i < activators.size(); i++) {
            Pair<Type, Resolver<Object>> activator = activators.get(i);
            if (pairFor(type).matches(activator)) {
                return (Resolver<T>) activators.remove(i).second();
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
