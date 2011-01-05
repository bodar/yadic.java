package com.googlecode.yadic.examples;

import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;

public class NodeResolver implements Resolver<Node>{
    public Node resolve(Type type) throws Exception {
        return new RootNode();
    }
}
