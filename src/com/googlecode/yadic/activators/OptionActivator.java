package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Exceptions;
import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;

public class OptionActivator implements Callable<Option> {
    private final Resolver resolver;
    private final Type type;

    public OptionActivator(final Type type, final Resolver resolver) {
        this.resolver = resolver;
        this.type = type;
    }

    public Option call() throws Exception {
        try {
            return option(resolver.resolve(type));
        } catch (ContainerException e) {
            return none();
        }
    }
}