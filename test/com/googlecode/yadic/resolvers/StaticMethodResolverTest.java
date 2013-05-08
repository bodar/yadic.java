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
        StaticMethodResolver<Integer> resolver = StaticMethodResolver.staticMethodResolver(containerWith("1"), Integer.class);
        assertThat(resolver.resolve(Integer.class), is(1));
    }

    @Test
    public void supportsCreatingObjectsViaStaticFromStringEnum() throws Exception {
        StaticMethodResolver<TimeUnit> resolver = StaticMethodResolver.staticMethodResolver(containerWith("NANOSECONDS"), TimeUnit.class);
        assertThat(resolver.resolve(TimeUnit.class), is(TimeUnit.NANOSECONDS));
    }

    @Test
    public void supportsStaticFactoryMethodWithSameName() throws Exception {
        StaticMethodResolver<MyStaticMethodClass> resolver = StaticMethodResolver.staticMethodResolver(containerWith("foobar"), MyStaticMethodClass.class);
        MyStaticMethodClass staticMethodClass = resolver.resolve(MyStaticMethodClass.class);
        assertThat(staticMethodClass, is(notNullValue()));
        assertThat(staticMethodClass.constructedBy, is("myStaticMethodClass1"));
    }

    @Test
    public void choosesLargestArity() throws Exception {
        StaticMethodResolver<MyStaticMethodClass> resolver = StaticMethodResolver.staticMethodResolver(containerWith("foobar").addInstance(Integer.class, 1), MyStaticMethodClass.class);
        MyStaticMethodClass staticMethodClass = resolver.resolve(MyStaticMethodClass.class);
        assertThat(staticMethodClass, is(notNullValue()));
        assertThat(staticMethodClass.constructedBy, is("myStaticMethodClass2"));
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
