package com.googlecode.yadic.activators;

import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

public class MissingResolver implements Resolver {
    public Object resolve(Type type) throws Exception {
        throw new ContainerException(type.toString() + " not found in container");
    }
}
