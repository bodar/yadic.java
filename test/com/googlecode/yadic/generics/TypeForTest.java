package com.googlecode.yadic.generics;

import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypeForTest {
    @Test
    public void capturesGenericSignature(){
        assertThat(new TypeFor<String>() {{}}.get(), is((Type)String.class));
    }
}
