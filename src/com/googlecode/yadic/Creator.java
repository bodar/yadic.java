package com.googlecode.yadic;

import java.lang.reflect.Type;

public interface Creator {
    <T> T create(Type type) throws Exception;
}
