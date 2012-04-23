package com.googlecode.yadic.closeable;

import java.io.Closeable;
import java.lang.reflect.Type;

public interface CloseableMap<Self extends CloseableMap<Self>> extends Closeable {
    Self addCloseable(Type type, Closeable closeable);

    <T> Self removeCloseable(Type type);
}
