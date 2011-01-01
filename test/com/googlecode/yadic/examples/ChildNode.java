package com.googlecode.yadic.examples;

public class ChildNode implements Node {
    private final RootNode parent;

    public ChildNode(RootNode parent) {
        this.parent = parent;
    }

    public ChildNode() {
        this(null);
    }

    public Node parent() {
        return parent;
    }
}
