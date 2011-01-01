package com.googlecode.yadic.examples;

public class DecoratedNode implements Node {
    private final Node dependency;

    public DecoratedNode(Node dependency) {
        this.dependency = dependency;
    }

    public Node parent() {
        return dependency;
    }
}
