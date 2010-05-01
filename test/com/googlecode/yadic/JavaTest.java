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
    public void shouldSupportUserDefinedResolver() {
        final int[] count = {0};
        Container container = new SimpleContainer(new Resolver(){
            public Object resolve(Class aClass) {
                count[0]++;
                return new Dependacy();
            }
        });
        container.add(Depends.class);
        assertNotNull(container.resolveType(Depends.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldSupportDifferentActivators() {
        Container container = new SimpleContainer();
        final int[] count = {0};
        container.add(NoDependancies.class, new Activator<NoDependancies>(){
            public NoDependancies activate() {
                count[0]++;
                return new NoDependancies();
            }
        });
        assertNotNull(container.resolveType(NoDependancies.class));
        assertEquals(1, count[0]);
    }

    static public class NoDependancies {}
    static public class Dependacy {}
    static public class Depends {
        public Depends(Dependacy dependacy) {
        }
    }

}
