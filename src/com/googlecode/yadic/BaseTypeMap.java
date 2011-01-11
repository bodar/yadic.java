package com.googlecode.yadic;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.yadic.resolvers.ClosableResolver;
import com.googlecode.yadic.resolvers.ObjectResolver;
import com.googlecode.yadic.resolvers.Resolvers;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.LazyCallable1.lazy;
import static com.googlecode.yadic.generics.Types.matches;
import static com.googlecode.yadic.resolvers.Resolvers.*;

public class BaseTypeMap implements TypeMap {
    private final List<Pair<Type, Resolver<Object>>> activators = new ArrayList<Pair<Type, Resolver<Object>>>();
    protected final Resolver parent;
    private List<Closeable> closeables = new ArrayList<Closeable>();

    public BaseTypeMap(Resolver parent) {
        this.parent = parent;
        add(Object.class, new ObjectResolver());
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
        ClosableResolver closableResolver = new ClosableResolver<Object>(create(concrete, this));
        return add(type, closableResolver, closableResolver);
    }

    public TypeMap add(Type type, Class<? extends Resolver> resolverClass) {
        return add(type, activator(this, resolverClass));
    }

    public TypeMap add(Type type, Resolver<?> resolver) {
        add(type, resolver, ignore());
        return this;
    }

    @SuppressWarnings("unchecked")
    public TypeMap add(Type type, Resolver<?> resolver, Closeable closeable) {
        if (contains(type)) {
            throw new ContainerException(type.toString() + " already added to container");
        }
        activators.add(Pair.<Type, Resolver<Object>>pair(type, asResolver(lazy(asCallable1(resolver)))));
        closeables.add(closeable);
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

    public void close() throws IOException {
        sequence(closeables).forEach(Resolvers.close());
    }
}
