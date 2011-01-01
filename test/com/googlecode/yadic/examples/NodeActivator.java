package com.googlecode.yadic.examples;

import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class NodeActivator implements Callable<Node> {
    final Container container;

    public NodeActivator(Container container) {
        this.container = container;
    }

    public Node call() {
        return container.get(Node.class);
    }
}
