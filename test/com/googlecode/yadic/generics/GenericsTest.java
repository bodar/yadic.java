package com.googlecode.yadic.generics;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.examples.ClassWithOption;
import com.googlecode.yadic.examples.GenericType;
import com.googlecode.yadic.examples.UsesGenericType;
import org.junit.Test;

import static com.googlecode.yadic.generics.Types.parameterizedType;
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
    public void containerShouldSupportOptions() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.add(ClassWithOption.class);
        assertThat(container.resolve(ClassWithOption.class), is(notNullValue()));
    }

}
