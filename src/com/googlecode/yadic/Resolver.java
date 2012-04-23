package com.googlecode.yadic;

import java.lang.reflect.Type;

public interface Resolver<T> {
    T resolve(Type type) throws Exception;
}
