package com.googlecode.yadic.examples;

import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class Creator implements Callable<Thing> {
    final Container container;

    public Creator(Container container) {
        this.container = container;
    }

    public Thing call() {
        return container.get(Thing.class);
    }
}
