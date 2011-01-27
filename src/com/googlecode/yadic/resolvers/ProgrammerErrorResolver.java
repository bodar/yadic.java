package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

public class ProgrammerErrorResolver<T> implements Resolver<T> {

    private final Type aType;

    public ProgrammerErrorResolver(Type aType) {
        this.aType = aType;
    }

    public T resolve(Type type) throws Exception {
        throw new ContainerException("Tried to resolve " + aType +"- this most likely suggests programmer error.");
    }
}
