package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

public class ObjectResolver implements Resolver<Object> {
    public Object resolve(Type type) throws Exception {
        throw new ContainerException("Tried to resolve Object.class- this most likely suggests programmer error.");
    }
}
