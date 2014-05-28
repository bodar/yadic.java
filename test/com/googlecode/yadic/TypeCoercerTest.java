package com.googlecode.yadic;

import org.junit.Test;

import java.util.Arrays;

import static com.googlecode.totallylazy.Assert.assertTrue;

public class TypeCoercerTest {
    @Test
    public void supportsInstanceMethods() throws Exception {
        String input = "foo";
        Resolver<Object> resolver = new TypeCoercer(input);
        byte[] bytes = (byte[]) resolver.resolve(byte[].class);
        assertTrue(Arrays.equals(bytes, input.getBytes("UTF-8")));
    }
}
