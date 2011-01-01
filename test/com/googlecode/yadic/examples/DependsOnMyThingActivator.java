package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class DependsOnMyThingActivator implements Callable<DependsOnMyThing> {
    private final MyThing dependency;

    public DependsOnMyThingActivator(MyThing dependency) {
        this.dependency = dependency;
    }

    public DependsOnMyThing call() {
        return new DependsOnMyThing(dependency);
    }
}
