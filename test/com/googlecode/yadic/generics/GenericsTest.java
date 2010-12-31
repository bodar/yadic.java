package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.generics.GenericType;
import org.junit.Ignore;
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

    public static  class UsesGenericType {
        private final GenericType<Integer> value;

        public UsesGenericType(GenericType<Integer> value) {
            this.value = value;
        }

        public GenericType<Integer> getValue() {
            return value;
        }
    }

    @Test
    public void containerShouldSupportOptions() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.add(ClassWithOption.class);
        assertThat(container.resolve(ClassWithOption.class), is(notNullValue()));
    }

    public static class ClassWithOption {
        public ClassWithOption(Option<String> optional) {}
    }

}
