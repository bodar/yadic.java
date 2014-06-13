package com.googlecode.yadic.collections;

import com.googlecode.yadic.examples.Node;
import com.googlecode.yadic.examples.RootNode;
import org.junit.Test;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.yadic.collections.Activator.activator;
import static com.googlecode.yadic.collections.Activators.*;
import static com.googlecode.yadic.collections.ListResolver.listResolver;

public class ActivatorsTest {
    @Test
    public void supportsConstructors() throws Exception {
        assertThat(listResolver(activator(types(Node.class), constructor(RootNode.class), destructor(RootNode.class))).
                resolve(Node.class), instanceOf(RootNode.class));
    }
}
