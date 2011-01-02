package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.None;
import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.examples.*;
import org.junit.Test;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.yadic.generics.Types.parameterizedType;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class GenericsTest {
    @Test
    public void containerShouldSupportRandomGenericClasses() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.add(parameterizedType(GenericType.class, Integer.class), GenericType.class);
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.getValue().getInstance(), is(1));
    }

    @Test
    public void containerShouldSupportSomeOption() throws Exception {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);
        container.add(FlexibleNode.class);
        assertThat(container.get(FlexibleNode.class).parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void containerShouldSupportNoneOption() throws Exception {
        Container container = new SimpleContainer();
        container.add(FlexibleNode.class);
        Option<Node> none = Option.none(Node.class);
        assertThat(container.get(FlexibleNode.class).optionalParent(), is(none));
    }
}