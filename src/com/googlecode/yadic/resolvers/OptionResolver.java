package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Exceptions.causes;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.instanceOf;

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
            if(predicate.matches(firstNonContainerException(e))){
                return none();
            }
            throw e;
        }
    }

    private Throwable firstNonContainerException(ContainerException e) {
        return causes(e).dropWhile(instanceOf(ContainerException.class)).headOption().getOrNull();
    }
}