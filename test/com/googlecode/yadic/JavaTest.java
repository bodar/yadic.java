package com.googlecode.yadic;

import org.junit.Test;

import java.util.concurrent.Callable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JavaTest {
    @Test
    public void shouldThrowWhenClassDoesNotHaveAPublicConstructor() {
        try {
            Container container = new SimpleContainer();
            container.add(PrivateClass.class);
            container.get(PrivateClass.class);
        } catch (ContainerException e) {
            assertThat(e.getMessage(), is(PrivateClass.class.getName() + " does not have a public constructor") );
        }
    }

    @Test
    public void shouldBeCallableFromJava() {
        Container container = new SimpleContainer();
        container.add(NoDependancies.class);
        assertNotNull(container.get(NoDependancies.class));
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
        assertNotNull(container.get(Depends.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldSupportDifferentCallables() {
        Container container = new SimpleContainer();
        final int[] count = {0};
        container.addActivator(NoDependancies.class, new NoDependanciesCallable(count));
        assertNotNull(container.get(NoDependancies.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldBeAbleToGetTheCallableForAType() throws Exception {
        Container container = new SimpleContainer();
        container.add(NoDependancies.class);
        assertNotNull(container.getActivator(NoDependancies.class));
    }

    @Test
    public void shouldBeAbleToReregisterAClassAgainstAParentInterface() throws Exception {
        Container container = new SimpleContainer();
        container.add(SomeInterfaceImpl.class);
        container.addActivator(SomeInterface.class, container.getActivator(SomeInterfaceImpl.class));
        assertNotNull(container.getActivator(SomeInterface.class));
    }


    static public interface SomeInterface{}
    static public class SomeInterfaceImpl implements SomeInterface{}

    static public class NoDependancies {}
    static public class Dependancy {}
    static public class Depends {
        public Depends(Dependancy dependancy) {
        }
    }

    static private class PrivateClass{
        private PrivateClass() {
        }
    }

    private static class NoDependanciesCallable implements Callable<NoDependancies> {
        private final int[] count;

        public NoDependanciesCallable(int[] count) {
            this.count = count;
        }

        public NoDependancies call() {
            count[0]++;
            return new NoDependancies();
        }
    }
}
