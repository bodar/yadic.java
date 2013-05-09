package com.googlecode.yadic.closeable;

import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.examples.ClosableStringResolver;
import com.googlecode.yadic.examples.SomeClosableClass;
import com.googlecode.yadic.examples.ThrowingClosableClass;
import com.googlecode.yadic.examples.ThrowingClosableResolver;
import com.googlecode.yadic.resolvers.MissingResolver;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class CloseableTypeMapTest {
    @Test
    public void ifClassIsNotClosableButTheActivatorIsCallCloseOnTheActivator() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addType(AtomicBoolean.class, AtomicBoolean.class);
        typeMap.addResolver(String.class, ClosableStringResolver.class);
        AtomicBoolean closed = (AtomicBoolean) typeMap.resolve(AtomicBoolean.class);
        assertThat(closed.get(), is(false));
        String resolve = (String) typeMap.resolve(String.class);
        typeMap.close();
        assertThat(closed.get(), is(true));
    }

    @Test
    public void ifClassImplementsClosableThenClosingTheTypeMapWillCloseTheObject() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addType(SomeClosableClass.class, SomeClosableClass.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    @Test
    public void addingAClosableActivatorClassForAClosableTypeWillCallCloseOnTheActivatorNotTheInstance() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());

        AtomicBoolean closed = new AtomicBoolean(false);
        typeMap.addResolver(SomeClosableClass.class, new CustomResolver(closed));

        final SomeClosableClass instance = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(instance.closed, CoreMatchers.is(false));

        typeMap.close();

        assertThat(closed.get(), CoreMatchers.is(true));
        assertThat(instance.closed, CoreMatchers.is(false));
    }

    @Test
    public void removingTheTypeAlsoStopsInstanceBeingClosed() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addType(SomeClosableClass.class, SomeClosableClass.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        typeMap.remove(SomeClosableClass.class);
        typeMap.close();
        assertThat(closable.closed, is(false));
    }

    @Test
    public void doesNotResolveAResolverWhenClosing() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        CustomResolver resolver = new CustomResolver(new AtomicBoolean());
        typeMap.addResolver(SomeClosableClass.class, resolver);
        assertThat(resolver.resolved(), is(false));
        assertThat(resolver.closed.get(), is(false));
        typeMap.close();
        assertThat(resolver.resolved(), is(false));
        assertThat(resolver.closed.get(), is(false));
    }

    @Test
    public void doesNotCloseAResolverIfTheResolverFailedToBeCreated() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addResolver(SomeClosableClass.class, UnsatisfiableResolver.class);
        try {
            typeMap.resolve(SomeClosableClass.class);
        } catch (ContainerException e) {
            // ignore
        }
        typeMap.close();
    }

    @Test
    public void canUseCustomResolverAndStillSupportClosingResource() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        CustomResolver resolver = new CustomResolver(new AtomicBoolean());
        typeMap.addResolver(SomeClosableClass.class, resolver);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);

        assertThat(resolver.closed.get(), is(false));
        assertThat(closable.closed, is(false));

        typeMap.close();

        assertThat(closable.closed, is(false));
        assertThat(resolver.closed.get(), is(true));
    }

    @Test
    public void canUseCustomResolverAndStillSupportClosingResourceEvenWhenActivatorNeedsToBeInstantiated() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addType(AtomicBoolean.class, AtomicBoolean.class);
        typeMap.addResolver(SomeClosableClass.class, CustomResolver.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        AtomicBoolean closed = (AtomicBoolean) typeMap.resolve(AtomicBoolean.class);

        assertThat(closed.get(), is(false));
        assertThat(closable.closed, is(false));

        typeMap.close();

        assertThat(closable.closed, is(false));
        assertThat(closed.get(), is(true));
    }
    
    @Test
    public void closeAllClosablesEvenWhenPreviousCloseMethodsThrowException() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addResolver(Integer.class, ThrowingClosableResolver.forType(1));
        typeMap.addType(ThrowingClosableClass.class, ThrowingClosableClass.class);
        typeMap.addType(SomeClosableClass.class, SomeClosableClass.class);

        SomeClosableClass someClosableClass = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        typeMap.resolve(Integer.class);
        typeMap.resolve(ThrowingClosableClass.class);

        assertThat(someClosableClass.closed, is(false));

        typeMap.close();

        assertThat(someClosableClass.closed, is(true));
    }

    public static class CustomResolver implements Resolver<SomeClosableClass>, Closeable {
        public final AtomicBoolean closed;
        private SomeClosableClass closable;

        public CustomResolver(AtomicBoolean closed) {
            this.closed = closed;
        }

        public SomeClosableClass resolve(Type type) throws Exception {
            closable = new SomeClosableClass();
            return closable;
        }

        public void close() throws IOException {
            closed.set(true);
            if (closable == null) {
                fail("Should never call close if resolve was not called first");
            }
        }

        public boolean resolved() {
            return closable != null;
        }
    }

    public static class UnsatisfiableResolver implements Resolver<SomeClosableClass>, Closeable {
        private final String value;

        public UnsatisfiableResolver(String value) {
            this.value = value;
        }

        public SomeClosableClass resolve(Type type) throws Exception {
            throw new AssertionError("Should never be able to create");
        }

        public void close() throws IOException {

        }
    }

}
