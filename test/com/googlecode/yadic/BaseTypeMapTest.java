package com.googlecode.yadic;

import com.googlecode.yadic.resolvers.MissingResolver;
import org.junit.Test;

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
