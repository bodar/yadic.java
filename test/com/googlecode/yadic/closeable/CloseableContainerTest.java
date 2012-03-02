package com.googlecode.yadic.closeable;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.examples.ClosableStringCallable;
import com.googlecode.yadic.examples.SomeClosableClass;
import com.googlecode.yadic.examples.SomeClosableClassActivator;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;

public class CloseableContainerTest {
    @Test
    public void registeringContainerDoesNotCauseStackOverflowWhenClosing() throws Exception {
        Container container = Containers.selfRegister(CloseableContainer.closeableContainer());
        container.get(Container.class);
        Closeables.close(container);
    }

    @Test
    public void canStopAndInstanceFromBeingClosed() throws Exception {
        CloseableContainer container = CloseableContainer.closeableContainer();

        SomeClosableClass instance = new SomeClosableClass();

        container.addInstance(SomeClosableClass.class, instance);
        container.removeCloseable(SomeClosableClass.class);

        container.get(SomeClosableClass.class);

        assertThat(instance.closed, CoreMatchers.is(false));
        container.close();
        assertThat(instance.closed, CoreMatchers.is(false));
    }

    @Test
    public void addingAClosableActivatorClassForAClosableTypeWillCallCloseOnTheActivatorNotTheInstance() throws Exception {
        CloseableContainer container = CloseableContainer.closeableContainer();
        AtomicBoolean activatorClosed = new AtomicBoolean(false);
        container.addInstance(AtomicBoolean.class, activatorClosed);
        container.addActivator(SomeClosableClass.class, CalledCloseActivator.class);

        final SomeClosableClass instance = container.get(SomeClosableClass.class);

        assertThat(instance.closed, CoreMatchers.is(false));
        assertThat(activatorClosed.get(), CoreMatchers.is(false));
        container.close();
        assertThat(instance.closed, CoreMatchers.is(false));
        assertThat(activatorClosed.get(), CoreMatchers.is(true));
    }

    @Test
    public void addingAClosableActivatorClassForANonClosableTypeWillCallCloseOnTheActivatorNotTheInstance() throws Exception {
        CloseableContainer container = CloseableContainer.closeableContainer();
        AtomicBoolean activatorClosed = new AtomicBoolean(false);
        container.addInstance(AtomicBoolean.class, activatorClosed);
        container.addActivator(String.class, ClosableStringCallable.class);

        container.get(String.class);

        assertThat(activatorClosed.get(), CoreMatchers.is(false));
        container.close();
        assertThat(activatorClosed.get(), CoreMatchers.is(true));
    }

    @Test
    public void addingAClosableActivatorInstanceForAClosableTypeWillCallCloseOnTheActivatorNotTheInstance() throws Exception {
        CloseableContainer container = CloseableContainer.closeableContainer();
        SomeClosableClassActivator activator = new SomeClosableClassActivator();
        container.addActivator(SomeClosableClass.class, activator);

        final SomeClosableClass instance = container.get(SomeClosableClass.class);

        assertThat(instance.closed, CoreMatchers.is(false));
        assertThat(activator.closed, CoreMatchers.is(false));
        container.close();
        assertThat(instance.closed, CoreMatchers.is(false));
        assertThat(activator.closed, CoreMatchers.is(true));
    }

    @Test
    public void addingANonClosableActivatorForAClosableTypeWillStillCloseOnShutdown() throws Exception {
        CloseableContainer container = CloseableContainer.closeableContainer();
        container.addActivator(SomeClosableClass.class, new Callable<SomeClosableClass>() {
            public SomeClosableClass call() throws Exception {
                return new SomeClosableClass();
            }
        });

        final SomeClosableClass instance = container.get(SomeClosableClass.class);

        assertThat(instance.closed, CoreMatchers.is(false));
        container.close();
        assertThat(instance.closed, CoreMatchers.is(true));
    }

    public static class CalledCloseActivator implements Callable<SomeClosableClass>, Closeable {
        private final AtomicBoolean called;

        public CalledCloseActivator(AtomicBoolean called) {
            this.called = called;
        }

        public SomeClosableClass call() throws Exception {
            return new SomeClosableClass();
        }

        public void close() throws IOException {
            called.set(true);
        }
    }
}
