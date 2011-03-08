package com.googlecode.yadic;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypeCoercerTest {
    @Test
    public void supportsInstanceMethods() throws Exception {
        String input = "foo";
        Resolver<Object> resolver = new TypeCoercer(input);
        byte[] bytes = (byte[]) resolver.resolve(byte[].class);
        assertThat(bytes, is(input.getBytes()));
    }

}
