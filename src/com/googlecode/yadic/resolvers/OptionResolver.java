package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.sequence;

public class OptionResolver implements Resolver<Option> {
    private final Resolver resolver;
    private final Predicate<? super Throwable> predicate;

    public OptionResolver(final Resolver resolver, Predicate<? super Throwable> predicate) {
        this.resolver = resolver;
        this.predicate = predicate;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Option resolve(Type type) throws Exception {
        try {
            return option(resolver.resolve(((ParameterizedType) type).getActualTypeArguments()[0]));
        } catch (ContainerException e) {
            Sequence<Throwable> causes = sequence(causes(e));
            if (causes.exists(predicate) || causes.isEmpty()) {
                return none();
            }
            throw e;
        }
    }

    private static List<Throwable> causes(ContainerException containerException) {
        List<Throwable> throwables = new ArrayList<Throwable>();
        for (Exception exception : containerException.getCauses()) {
            if(exception instanceof ContainerException){
                throwables.addAll(causes((ContainerException) exception));
            } else {
                throwables.add(exception);
            }
        }
        return throwables;
    }
}