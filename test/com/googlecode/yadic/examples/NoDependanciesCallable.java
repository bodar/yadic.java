package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class NoDependanciesCallable implements Callable<NoDependencies> {
    private final int[] count;

    public NoDependanciesCallable(int[] count) {
        this.count = count;
    }

    public NoDependencies call() {
        count[0]++;
        return new NoDependencies();
    }
}
