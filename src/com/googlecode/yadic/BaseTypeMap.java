package com.googlecode.yadic;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.yadic.resolvers.ProgrammerErrorResolver;
import com.googlecode.yadic.resolvers.Resolvers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.yadic.generics.Types.matches;
import static com.googlecode.yadic.resolvers.LazyResolver.lazy;
import static com.googlecode.yadic.resolvers.Resolvers.*;

public class BaseTypeMap implements TypeMap {
    private final List<Pair<Type, Resolver<Object>>> activators = new ArrayList<Pair<Type, Resolver<Object>>>();
    protected final Resolver<?> parent;

    public BaseTypeMap(Resolver<?> parent) {
        this.parent = parent;
        addType(Object.class, new ProgrammerErrorResolver(Object.class));
    }

    public Object resolve(Type type) throws Exception {
        if (!contains(type)) {
            return parent.resolve(type);
        }
        return Resolvers.resolve(getResolver(type), type);
    }

    public <T> T create(Type type) throws Exception {
        return cast(Resolvers.create(type, this).resolve(type));
    }

    @SuppressWarnings("unchecked")
    public <T> Resolver<T> getResolver(Type type) {
        return (Resolver<T>) find(activators, pairFor(type));
    }

    public TypeMap addType(Type type, Type concrete) {
        return addType(type, Resolvers.create(concrete, this));
    }

    public TypeMap decorateType(final Type anInterface, final Type concrete) {
        return addType(anInterface, decorator(this, anInterface, concrete));
    }

    public TypeMap addType(Type type, Class<? extends Resolver> resolverClass) {
        return addType(type, activator(this, resolverClass));
    }

    @SuppressWarnings("unchecked")
    public TypeMap addType(Type type, Resolver<?> resolver) {
        if (contains(type)) {
            throw new ContainerException(type.toString() + " already added to container");
        }
        activators.add(Pair.<Type, Resolver<Object>>pair(type, lazy(resolver)));
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

    public static LogicalPredicate<First<Type>> pairFor(Type type) {
        return where(first(Type.class), matches(type));
    }

    public static <A, B> B find(final Iterable<Pair<A, B>> iterable, Predicate<First<A>> predicate) {
        return sequence(iterable).find(predicate).map(Callables.<B>second()).get();
    }


}
