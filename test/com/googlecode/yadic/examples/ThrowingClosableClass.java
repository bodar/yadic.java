package com.googlecode.yadic.examples;

import java.io.Closeable;
import java.io.IOException;

public class ThrowingClosableClass implements Closeable {
    public void close() throws IOException {
        throw new RuntimeException();
    }
}
