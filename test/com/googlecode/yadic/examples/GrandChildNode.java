package com.googlecode.yadic.examples;

public class GrandChildNode implements Node {
    private final ChildNode parent;

    public GrandChildNode(ChildNode parent) {
        this.parent = parent;
    }

    public Node parent() {
        return parent;
    }
}
