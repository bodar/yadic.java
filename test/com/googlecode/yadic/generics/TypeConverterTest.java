package com.googlecode.yadic.generics;

import com.googlecode.yadic.examples.Instance;
import org.junit.Test;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TypeConverterTest {
    @Test
    public void shouldHandleSimpleTypes() throws Exception {
        Map<TypeVariable, Type> map = TypeConverter.typeMap(new TypeFor<Instance<Integer>>() {}.get(), Instance.class);
        assertThat(map.toString(), is("{T=class java.lang.Integer}"));
    }
}
