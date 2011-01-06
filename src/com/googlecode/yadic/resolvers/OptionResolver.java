package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;

public class OptionResolver implements Resolver<Option> {
    private final Resolver resolver;

    public OptionResolver(final Resolver resolver) {
        this.resolver = resolver;
    }

    public Option resolve(Type type) throws Exception {
        try {
            return option(resolver.resolve(((ParameterizedType) type).getActualTypeArguments()[0]));
        } catch (ContainerException e) {
            return none();
        }
    }
}