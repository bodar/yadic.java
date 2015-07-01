package com.googlecode.yadic;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.functions.Callables;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.predicates.Predicates;
import com.googlecode.yadic.resolvers.ProgrammerErrorResolver;
import com.googlecode.yadic.resolvers.Resolvers;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.googlecode.totallylazy.functions.Callables.first;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.reflection.Types.matches;
import static com.googlecode.yadic.resolvers.LazyResolver.lazy;
import static com.googlecode.yadic.resolvers.Resolvers.activator;
import static com.googlecode.yadic.resolvers.Resolvers.decorator;

public class BaseTypeMap implements TypeMap {
    private final List<Pair<Type, Resolver<Object>>> activators = new CopyOnWriteArrayList<Pair<Type, Resolver<Object>>>();
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

    public <T> T create(Type type) throws ContainerException {
        try {
            return Unchecked.<T>cast(Resolvers.create(type, this).resolve(type));
        } catch (Exception e) {
            throw new ContainerException("Could not create " + type, e);
        }
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
        return addType(type, activator(this, Unchecked.<Class<Resolver<Object>>>cast(resolverClass)));
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
        return this.<T>removeOption(type).get();
    }

    public <T> Option<Resolver<T>> removeOption(Type type) {
        for (int i = 0; i < activators.size(); i++) {
            Pair<Type, Resolver<Object>> activator = activators.get(i);
            if (pairFor(type).matches(activator)) {
                return some(Unchecked.<Resolver<T>>cast(activators.remove(i).second()));
            }
        }
        return none();
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

    @Override
    public Iterator<Type> iterator() {
        return sequence(activators).map(first(Type.class)).filter(Predicates.<Type>is(Object.class).not()).iterator();
    }
}
