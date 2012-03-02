package com.googlecode.yadic.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClosableStringCallable implements Callable<String>, Closeable {
    public final AtomicBoolean closed;

    public ClosableStringCallable(AtomicBoolean closed) {
        this.closed = closed;
    }

    public void close() throws IOException {
        closed.set(true);
    }

    public String call() throws Exception {
        return "resolve called";
    }
}
