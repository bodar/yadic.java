package com.googlecode.yadic.generics;

import org.junit.Test;

import static com.googlecode.yadic.generics.Types.equalTo;
import static com.googlecode.yadic.generics.Types.parameterizedType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypesTest {
    @Test
    public void supportsEquality() throws Exception {
        assertThat(equalTo(Integer.class, Integer.class), is(true));
        assertThat(equalTo(Integer.class, String.class), is(false));
        assertThat(equalTo(parameterizedType(GenericType.class, Integer.class),
                parameterizedType(GenericType.class, Integer.class)), is(true));
    }
}
