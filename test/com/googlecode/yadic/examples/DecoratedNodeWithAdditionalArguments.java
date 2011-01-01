package com.googlecode.yadic.examples;

public class DecoratedNodeWithAdditionalArguments implements Node {
    private final Node dependency;

    public DecoratedNodeWithAdditionalArguments(Node dependency, String additionalArgument) {
        this.dependency = dependency;
    }

    public Node parent() {
        return dependency;
    }
}
