package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class MyThingActivator implements Callable<GrandChildNode> {
    public GrandChildNode call() {
        return new GrandChildNode(null);
    }
}
