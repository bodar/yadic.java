package com.googlecode.yadic.examples;

import java.util.concurrent.Callable;

public class DecorateNodeActivator implements Callable<Node> {

    private final Node node;

    public DecorateNodeActivator(Node node) {
        this.node = node;
    }

    public Node call() throws Exception {
        return new DecoratedNode(node);
    }
}
