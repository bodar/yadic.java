package com.googlecode.yadic.examples;

import com.googlecode.totallylazy.Option;

public class FlexibleNode implements Node{
    private final Option<Node> parent;

    public FlexibleNode(Option<Node> parent) {
        this.parent = parent;
    }

    public Node parent() {
        return parent.get();
    }

    public Option<Node> optionalParent() {
        return parent;
    }
}
