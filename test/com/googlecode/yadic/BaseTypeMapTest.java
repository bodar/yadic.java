package com.googlecode.yadic;

import com.googlecode.yadic.examples.SomeClosableClass;
import com.googlecode.yadic.resolvers.MissingResolver;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class BaseTypeMapTest {
    @Test(expected = ContainerException.class)
    public void canNeverAddAnObjectType() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.addType(Object.class, Object.class);
    }

    @Test(expected = ContainerException.class)
    public void canNeverResolveAnObjectType() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.resolve(Object.class);
    }


}
