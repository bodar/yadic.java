package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

public class MissingResolver implements Resolver {
    public Object resolve(Type type) throws Exception {
        throw new ContainerException(type.toString() + " not found in container");
    }
}
