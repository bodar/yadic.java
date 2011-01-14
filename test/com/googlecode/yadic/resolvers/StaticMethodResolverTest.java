package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.SimpleContainer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticMethodResolverTest {

    @Test
    public void supportsCreatingObjectsViaStaticValueOfMethod() throws Exception {
        StaticMethodResolver resolver = new StaticMethodResolver(containerWith("1"));
        assertThat((Integer) resolver.resolve(Integer.class), is(1));
    }

    @Test
    public void supportsCreatingObjectsViaStaticFromStringEnum() throws Exception {
        StaticMethodResolver resolver = new StaticMethodResolver(containerWith("NANOSECONDS"));
        assertThat((TimeUnit) resolver.resolve(TimeUnit.class), is(TimeUnit.NANOSECONDS));
    }

    @Test
    public void supportsStaticFactoryMethodWithSameName() throws Exception {
        StaticMethodResolver resolver = new StaticMethodResolver(containerWith("foobar"));
        assertThat(resolver.resolve(MyStaticMethodClass.class), is(notNullValue()));
    }

    @Test(expected = ContainerException.class)
    public void ignoresSelfReferencingStaticMethods() throws Exception {
        Container resolver = new SimpleContainer();
        resolver.add(SelfReferencingClass.class);
        resolver.resolve(SelfReferencingClass.class);
    }

    private Container containerWith(String value) {
        return new SimpleContainer().addInstance(String.class, value);
    }


    private static class MyStaticMethodClass {
        private MyStaticMethodClass() {
        }

        public static MyStaticMethodClass myStaticMethodClass(String parameter) {
            return new MyStaticMethodClass();
        }
    }

    private static class SelfReferencingClass {
        private SelfReferencingClass() {
        }

        public static SelfReferencingClass myStaticMethodClass(SelfReferencingClass self) {
            return new SelfReferencingClass();
        }
    }

}
