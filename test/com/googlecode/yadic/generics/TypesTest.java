package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.reflection.TypeFor;
import com.googlecode.yadic.examples.GenericType;
import org.junit.Test;

import static com.googlecode.totallylazy.reflection.Types.equalTo;
import static com.googlecode.totallylazy.reflection.Types.matches;
import static com.googlecode.totallylazy.reflection.Types.parameterizedType;
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
        assertThat(matches(new TypeFor<Either<String, Option<Integer>>>() {{}}.get(),
                new TypeFor<Either<?, ?>>() {{}}.get()), is(true));
    }
}
