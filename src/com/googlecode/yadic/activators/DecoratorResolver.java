package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callers;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

public class DecoratorResolver implements Resolver {
    private final Type anInterface;
    private final Callable<?> existing;
    private final TypeMap typeMap;

    public DecoratorResolver(Type anInterface, Callable<?> existing, TypeMap typeMap) {
        this.anInterface = anInterface;
        this.existing = existing;
        this.typeMap = typeMap;
    }

    public Object resolve(Type type) {
        return type.equals(anInterface) ? Callers.call(existing) : typeMap.resolve(type);
    }
}
