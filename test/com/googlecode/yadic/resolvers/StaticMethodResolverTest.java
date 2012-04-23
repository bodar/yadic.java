package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.examples.MyStaticMethodClass;
import com.googlecode.yadic.examples.SelfReferencingClass;
import com.googlecode.yadic.examples.ThrowingClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticMethodResolverTest {

    @Test
    public void supportsCreatingObjectsViaStaticValueOfMethod() throws Exception {
        StaticMethodResolver resolver = new StaticMethodResolver(containerWith("1"), Integer.class);
        assertThat((Integer) resolver.resolve(Integer.class), is(1));
    }

    @Test
    public void supportsCreatingObjectsViaStaticFromStringEnum() throws Exception {
        StaticMethodResolver resolver = new StaticMethodResolver(containerWith("NANOSECONDS"), TimeUnit.class);
        assertThat((TimeUnit) resolver.resolve(TimeUnit.class), is(TimeUnit.NANOSECONDS));
    }

    @Test
    public void supportsStaticFactoryMethodWithSameName() throws Exception {
        StaticMethodResolver resolver = new StaticMethodResolver(containerWith("foobar"), MyStaticMethodClass.class);
        assertThat(resolver.resolve(MyStaticMethodClass.class), is(notNullValue()));
    }

    @Test(expected = ContainerException.class)
    public void ignoresSelfReferencingStaticMethods() throws Exception {
        Container resolver = new SimpleContainer();
        resolver.add(SelfReferencingClass.class);
        resolver.resolve(SelfReferencingClass.class);
    }

    @Test
    public void choosesFirstMethodThatDoesNotThrowAnException() throws Exception {
        Container resolver = containerWith("foo");
        resolver.add(ThrowingClass.class);
        resolver.resolve(ThrowingClass.class);
    }

    public static Container containerWith(String value) {
        return new SimpleContainer().addInstance(String.class, value);
    }

}
