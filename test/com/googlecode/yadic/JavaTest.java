package com.googlecode.yadic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JavaTest {
    @Test
    public void shouldBeCallableFromJava() {
        Container container = new SimpleContainer();
        container.add(NoDependancies.class);
        assertNotNull(container.resolveType(NoDependancies.class));
    }

    @Test
    public void shouldSupportDifferentActivators() {
        Container container = new SimpleContainer();
        final int[] count = {0};
        container.add(NoDependancies.class, new Activator(){
            public Object activate() {
                count[0]++;
                return new NoDependancies();
            }
        });
        assertNotNull(container.resolveType(NoDependancies.class));
        assertEquals(1, count[0]);
    }

    static public class NoDependancies {}

}
