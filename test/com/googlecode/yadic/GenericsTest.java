package com.googlecode.yadic;

import com.googlecode.totallylazy.Option;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class GenericsTest {
    //@Test
    public void containerShouldSupportRandomGenericClasses() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.add(GenericType.class);
        container.add(UsesGenericType.class);
        assertThat(container.resolve(UsesGenericType.class), is(notNullValue()));
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

    public static class GenericType<T> {
        private final T instance;

        public GenericType(T instance) {
            this.instance = instance;
        }

        public T getInstance() {
            return instance;
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
