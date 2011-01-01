package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class MyThingActivator implements Callable<MyThing> {
    public MyThing call() {
        return new MyThing(null);
    }
}
