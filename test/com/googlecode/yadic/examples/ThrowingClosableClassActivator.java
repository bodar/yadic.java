package com.googlecode.yadic.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ThrowingClosableClassActivator extends SomeClosableClassActivator{
    public void close() throws IOException {
        throw new ActivatorClosedCalled();
    }
}
