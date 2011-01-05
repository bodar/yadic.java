package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.examples.GenericType;
import org.junit.Test;

import static com.googlecode.yadic.generics.Types.equalTo;
import static com.googlecode.yadic.generics.Types.matches;
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
        assertThat(equalTo(new TypeFor<GenericType<Integer>>(){{}}.get(),
                parameterizedType(GenericType.class, Integer.class)), is(true));
        assertThat(equalTo(new TypeFor<Option<?>>(){{}}.get(),
                new TypeFor<Option<?>>(){{}}.get()), is(true));
    }

    @Test
    public void supportsMatchingWithWildcards() throws Exception {
        assertThat(matches(new TypeFor<Option<Integer>>() {{}}.get(),
                new TypeFor<Option<?>>() {{}}.get()), is(true));
    }
}
