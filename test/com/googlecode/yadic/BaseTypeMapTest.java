package com.googlecode.yadic;


import com.googlecode.yadic.examples.GenericType;
import com.googlecode.yadic.resolvers.MissingResolver;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Assert.assertTrue;
import static com.googlecode.totallylazy.Sequences.sequence;

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

    @Test
    public void isAbleToIterateThroughTypes() {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());

        typeMap.addType(GenericType.class, GenericType.class);
        typeMap.addType(Integer.class, new Resolver<Integer>() {
            @Override
            public Integer resolve(Type type) throws Exception {
                return 0;
            }
        });
        typeMap.addType(Long.class, LongResolver.class);

        assertTrue(sequence(typeMap).containsAll(sequence(
                GenericType.class, Integer.class, Long.class)));
    }

    public static class LongResolver implements Resolver<Long> {
        @Override
        public Long resolve(Type type) throws Exception {
            return 1L;
        }
    }
}
