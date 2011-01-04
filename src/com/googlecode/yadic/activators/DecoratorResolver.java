package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callers;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;

public class DecoratorResolver implements Resolver {
    private final Type anInterface;
    private final Callable1<Type, ?> existing;
    private final TypeMap typeMap;

    public DecoratorResolver(Type anInterface, Callable1<Type, ?> existing, TypeMap typeMap) {
        this.anInterface = anInterface;
        this.existing = existing;
        this.typeMap = typeMap;
    }

    public Object resolve(Type type) {
        return type.equals(anInterface) ? Callers.call(existing, type) : typeMap.resolve(type);
    }
}
