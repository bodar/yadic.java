package com.googlecode.yadic.examples;

public class ThrowingClass {
    private ThrowingClass() {
    }

    public static ThrowingClass blows(String foo) {
        throw new AssertionError();
    }

    public static ThrowingClass works(String foo) {
        return new ThrowingClass();
    }

    public static ThrowingClass alsoBlows(String foo) {
        throw new AssertionError();
    }
}
