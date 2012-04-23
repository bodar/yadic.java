package com.googlecode.yadic.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

public class SomeClosableClassActivator implements Callable<SomeClosableClass>, Closeable {
    public boolean closed = false;
    public SomeClosableClass call() throws Exception {
        return new SomeClosableClass();
    }

    public void close() throws IOException {
        closed = true;
    }
}
