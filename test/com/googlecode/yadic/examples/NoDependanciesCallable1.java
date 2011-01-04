package com.googlecode.yadic.examples;


import com.googlecode.totallylazy.Callable1;

import java.lang.reflect.Type;

public class NoDependanciesCallable1 implements Callable1<Type, NoDependencies> {
    private final int[] count;

    public NoDependanciesCallable1(int[] count) {
        this.count = count;
    }

    public NoDependencies call(Type type) {
        count[0]++;
        return new NoDependencies();
    }
}
