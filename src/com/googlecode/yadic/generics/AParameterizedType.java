package com.googlecode.yadic.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class AParameterizedType implements ParameterizedType {
    private final Type rawType;
    private final Type[] typeArguments;
    private final Type ownerType;

    public AParameterizedType(Type ownerType, Type rawType, Type... typeArguments) {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.typeArguments = typeArguments;
    }

    public Type[] getActualTypeArguments() {
        return typeArguments;
    }

    public Type getRawType() {
        return rawType;
    }

    public Type getOwnerType() {
        return ownerType;
    }
}
