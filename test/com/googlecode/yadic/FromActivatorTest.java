package com.googlecode.yadic;

import org.junit.Test;

import static com.googlecode.yadic.FromActivator.from;
import static org.junit.Assert.assertSame;

public class FromActivatorTest {
    @Test
    public void allowsRegisteringAnObjectWithTwoInterfaces() throws Exception {
        Container container = new SimpleContainer();
        container.add(SimpleContainerTest.SomeInterfaceImpl.class);
        container.addActivator(SimpleContainerTest.SomeInterface.class, from(container, SimpleContainerTest.SomeInterfaceImpl.class));
        final SimpleContainerTest.SomeInterfaceImpl someInterfaceImpl = container.get(SimpleContainerTest.SomeInterfaceImpl.class);
        final SimpleContainerTest.SomeInterface someInterface = container.get(SimpleContainerTest.SomeInterface.class);
        assertSame(someInterfaceImpl, someInterface);
    }

}
