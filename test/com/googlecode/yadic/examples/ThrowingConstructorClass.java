package com.googlecode.yadic.examples;

import java.util.Date;

public class ThrowingConstructorClass {

    public ThrowingConstructorClass(String foo) {
        throw new AssertionError();
    }

    public ThrowingConstructorClass(Date date) {
        // happy
    }

    public ThrowingConstructorClass(Integer foo) {
        throw new AssertionError();
    }
}
