package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.examples.ThrowingClass;
import com.googlecode.yadic.examples.ThrowingConstructorClass;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.Dates.date;

public class ConstructorResolverTest {
    @Test
    public void choosesFirstConstructorThatDoesNotThrowAnException() throws Exception {
        Container resolver = new SimpleContainer().addInstance(String.class, "Foo").addInstance(Integer.class, 1).addInstance(Date.class, date(2001, 1, 1));
        resolver.add(ThrowingConstructorClass.class);
        resolver.resolve(ThrowingConstructorClass.class);
    }
}
