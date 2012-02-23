package com.googlecode.yadic.closeable;

import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.examples.ActivatorClosedCalled;
import com.googlecode.yadic.examples.SomeClosableClass;
import com.googlecode.yadic.resolvers.MissingResolver;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class CloseableTypeMapTest {
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
        typeMap.addType(SomeClosableClass.class, new ThrowingClosableResolver());

        final SomeClosableClass instance = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);

        assertThat(instance.closed, CoreMatchers.is(false));

        try {
            typeMap.close();
            fail("Should have got exception from activator");
        } catch (ActivatorClosedCalled e) {
            assertThat(instance.closed, CoreMatchers.is(false));
        }
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
        CustomResolver resolver = new CustomResolver();
        typeMap.addType(SomeClosableClass.class, resolver);
        assertThat(resolver.resolved(), is(false));
        typeMap.close();
        assertThat(resolver.resolved(), is(false));
    }

    @Test
    public void doesNotCloseAResolverIfTheResolverFailedToBeCreated() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addType(SomeClosableClass.class, UnsatisfiableResolver.class);
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
        typeMap.addType(SomeClosableClass.class, new CustomResolver());
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    @Test
    public void canUseCustomResolverAndStillSupportClosingResourceEvenWhenActivatorNeedsToBeInstantiated() throws Exception {
        CloseableTypeMap typeMap = new CloseableTypeMap(new MissingResolver());
        typeMap.addType(SomeClosableClass.class, CustomResolver.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    public static class CustomResolver implements Resolver<SomeClosableClass>, Closeable {
        private SomeClosableClass closable;

        public SomeClosableClass resolve(Type type) throws Exception {
            closable = new SomeClosableClass();
            return closable;
        }

        public void close() throws IOException {
            if (closable == null) {
                fail("Should never call close if resolve was not called first");
            }
            closable.close();
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

    public static class ThrowingClosableResolver implements Resolver<SomeClosableClass>, Closeable {
        public SomeClosableClass resolve(Type type) throws Exception {
            return new SomeClosableClass();
        }

        public void close() throws IOException {
            throw new ActivatorClosedCalled();
        }
    }
}
