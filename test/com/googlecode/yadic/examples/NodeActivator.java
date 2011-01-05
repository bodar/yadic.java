package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class NodeActivator implements Callable<Node>{
    public Node call() throws Exception {
        return new RootNode();
    }
}
