package com.googlecode.yadic.generics;

import org.junit.Test;

import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Predicates.is;

public class TypeForTest {
    @Test
    public void capturesGenericSignature(){
        assertThat(new TypeFor<String>() {{}}.get(), is((Type)String.class));
    }
}
