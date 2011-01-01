package com.googlecode.yadic.examples;

public class DependsOnMyNode implements Node {
    private final GrandChildNode dependency;

    public DependsOnMyNode(GrandChildNode dependency) {
        this.dependency = dependency;
    }

    public Node parent() {
        return dependency;
    }
}
