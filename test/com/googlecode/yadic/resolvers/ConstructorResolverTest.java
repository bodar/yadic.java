package com.googlecode.yadic.resolvers;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.examples.SelfReferencingConstructorClass;
import com.googlecode.yadic.examples.ThrowingConstructorClass;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.time.Dates.date;

public class ConstructorResolverTest {
    @Test
    public void choosesFirstConstructorThatDoesNotThrowAnException() throws Exception {
        Container resolver = new SimpleContainer().addInstance(String.class, "Foo").addInstance(Integer.class, 1).addInstance(Date.class, date(2001, 1, 1));
        resolver.add(ThrowingConstructorClass.class);
        resolver.resolve(ThrowingConstructorClass.class);
    }

    @Test(expected = ContainerException.class)
    public void ignoresSelfReferencingConstructors() throws Exception {
        Container resolver = new SimpleContainer();
        resolver.add(SelfReferencingConstructorClass.class);
        resolver.resolve(SelfReferencingConstructorClass.class);
    }
}
