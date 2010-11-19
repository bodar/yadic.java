package com.googlecode.yadic;

import com.googlecode.totallylazy.Option;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;

public class OptionActivator implements Callable<Option> {
    private final Resolver resolver;
    private final Class<?> typeClass;

    public OptionActivator(final Class<?> typeClass, final Resolver resolver) {
        this.resolver = resolver;
        this.typeClass = typeClass;
    }

    public Option call() throws Exception {
        try {
            return option(resolver.resolve(typeClass));
        } catch (ContainerException e) {
            if(e.getCause() instanceof NoSuchElementException){
                return none();
            }
            throw e;
        }
    }
}