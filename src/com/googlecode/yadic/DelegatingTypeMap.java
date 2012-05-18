package com.googlecode.yadic;

import java.lang.reflect.Type;

public abstract class DelegatingTypeMap implements TypeMap{
    private final TypeMap map;

    public DelegatingTypeMap(TypeMap map) {
        this.map = map;
    }

    protected abstract TypeMap self();

    public TypeMap addType(Type type, Resolver<?> resolver) {
        map.addType(type, resolver);
        return self();
    }

    public TypeMap addType(Type type, Class<? extends Resolver> resolverClass) {
        map.addType(type, resolverClass);
        return self();
    }

    public TypeMap addType(Type type, Type concrete) {
        map.addType(type, concrete);
        return self();
    }

    public <T> Resolver<T> getResolver(Type type) {
        return map.getResolver(type);
    }

    public <T> Resolver<T> remove(Type type) {
        return map.remove(type);
    }

    public boolean contains(Type type) {
        return map.contains(type);
    }

    public TypeMap decorateType(Type anInterface, Type concrete) {
        map.decorateType(anInterface, concrete);
        return self();
    }

    public Object resolve(Type type) throws Exception {
        return map.resolve(type);
    }

    public <T> T create(Type type) throws Exception {
        return map.<T>create(type);
    }
}
