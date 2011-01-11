package com.googlecode.yadic;

import com.googlecode.yadic.resolvers.MissingResolver;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BaseTypeMapTest {
    @Test (expected = ContainerException.class)
    public void canNeverAddAnObjectType() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(Object.class, Object.class);
    }

    @Test (expected = ContainerException.class)
    public void canNeverResolveAnObjectType() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.resolve(Object.class);
    }

    @Test
    public void ifClassImplementsClosableThenClosingTheTypeMapWillCloseTheObject() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(SomeClosableClass.class, SomeClosableClass.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    public static class SomeClosableClass implements Closeable{
        public boolean closed = false;

        public void close() throws IOException {
            closed = true;
        }
    }
}
