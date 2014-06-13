package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.resolvers.MissingResolver;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Sequences.sequence;

public class ListResolver implements Resolver<Object>, AutoCloseable {
    private final Sequence<Activator<?>> list;
    private final Resolver<?> parent;

    private ListResolver(Iterable<? extends Activator<?>> list, Resolver<?> parent) {
        this.parent = parent;
        this.list = sequence(list);
    }

    public static ListResolver listResolver(Activator<?>... list) {
        return listResolver(new MissingResolver(), list);
    }

    public static ListResolver listResolver(Resolver<?> parent, Activator<?>... list) {
        return listResolver(parent, sequence(list));
    }

    public static ListResolver listResolver(Iterable<? extends Activator<?>> list) {
        return listResolver(new MissingResolver(), list);
    }

    public static ListResolver listResolver(Resolver<?> parent, Iterable<? extends Activator<?>> list) {
        return new ListResolver(list, parent);
    }

    @Override
    public Object resolve(Type type) throws Exception {
        return list.
                find(a -> a.matches(type)).
                map(a -> (Object) a.apply(type, list)).
                getOrElse(() -> parent.resolve(type));
    }

    @Override
    public void close() throws Exception {
        list.each(Activator::close);
    }
}
