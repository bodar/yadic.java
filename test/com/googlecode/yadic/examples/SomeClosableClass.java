package com.googlecode.yadic.examples;

import java.io.Closeable;
import java.io.IOException;

public class SomeClosableClass implements Closeable {
    public boolean closed = false;

    public void close() throws IOException {
        closed = true;
    }
}
