package com.googlecode.yadic.examples;

import com.googlecode.yadic.Resolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClosableStringResolver implements Resolver<String>, Closeable {
    public final AtomicBoolean closed;

    public ClosableStringResolver(AtomicBoolean closed) {
        this.closed = closed;
    }

    public void close() throws IOException {
        closed.set(true);
    }

    public String resolve(Type type) throws Exception {
        return "resolve called";
    }
}
