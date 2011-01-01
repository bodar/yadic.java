package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class DependsOnMyThingActivator implements Callable<DependsOnMyNode> {
    private final GrandChildNode dependency;

    public DependsOnMyThingActivator(GrandChildNode dependency) {
        this.dependency = dependency;
    }

    public DependsOnMyNode call() {
        return new DependsOnMyNode(dependency);
    }
}
