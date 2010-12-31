package com.googlecode.yadic;

import java.lang.reflect.Type;

public interface Resolver {
    Object resolve(Type type);
}
