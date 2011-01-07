package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Exceptions;
import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import static com.googlecode.totallylazy.Exceptions.causes;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.instanceOf;

public class OptionResolver implements Resolver<Option> {
    private final Resolver resolver;

    public OptionResolver(final Resolver resolver) {
        this.resolver = resolver;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Option resolve(Type type) throws Exception {
        try {
            return option(resolver.resolve(((ParameterizedType) type).getActualTypeArguments()[0]));
        } catch (ContainerException e) {
            if(firstNonContainerException(e) instanceof NoSuchElementException){
                return none();
            }
            throw e;
        }
    }

    private Throwable firstNonContainerException(ContainerException e) {
        return causes(e).dropWhile(instanceOf(ContainerException.class)).headOption().getOrNull();
    }
}