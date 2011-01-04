package com.googlecode.yadic.examples;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.yadic.Container;

import java.lang.reflect.Type;

public class NodeActivator implements Callable1<Type, Node> {
    final Container container;

    public NodeActivator(Container container) {
        this.container = container;
    }

    public Node call(Type type) {
        return container.get(Node.class);
    }
}
