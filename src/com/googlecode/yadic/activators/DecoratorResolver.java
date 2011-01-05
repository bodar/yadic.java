package com.googlecode.yadic.activators;

import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;

public class DecoratorResolver<T> implements Resolver<T> {
    private final Type anInterface;
    private final Resolver<T> existing;
    private final TypeMap typeMap;

    public DecoratorResolver(Type anInterface, Resolver<T> existing, TypeMap typeMap) {
        this.anInterface = anInterface;
        this.existing = existing;
        this.typeMap = typeMap;
    }

    public T resolve(Type type) throws Exception {
        return type.equals(anInterface) ? existing.resolve(type) : (T) typeMap.resolve(type);
    }
}
