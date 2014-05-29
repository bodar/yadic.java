package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

public class ListResolver implements Resolver<Object>, AutoCloseable {
    private final Sequence<Activator<?>> list;

    public ListResolver(Iterable<? extends Activator<?>> list) {
        this.list = Sequences.sequence(list);
    }

    @Override
    public Object resolve(Type type) throws Exception {
        return list.
                find(a -> a.matches(type)).
                get().
                apply(list);
    }

    @Override
    public void close() throws Exception {
        list.each(Activator::close);
    }

    public static ListResolver listResolver(Iterable<? extends Activator<?>> list) {
        return new ListResolver(list);
    }


}
