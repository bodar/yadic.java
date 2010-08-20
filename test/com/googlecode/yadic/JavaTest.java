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
                return new Dependancy();
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
        container.addActivator(NoDependancies.class, new NoDependanciesActivator(count));
        assertNotNull(container.resolveType(NoDependancies.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldBeAbleToGetTheActivatorForAType() throws Exception {
        Container container = new SimpleContainer();
        container.add(NoDependancies.class);
        assertNotNull(container.getActivator(NoDependancies.class));
    }


    static public class NoDependancies {}
    static public class Dependancy {}
    static public class Depends {
        public Depends(Dependancy dependancy) {
        }
    }

    private static class NoDependanciesActivator implements Activator<NoDependancies> {
        private final int[] count;

        public NoDependanciesActivator(int[] count) {
            this.count = count;
        }

        public NoDependancies activate() {
            count[0]++;
            return new NoDependancies();
        }
    }
}
