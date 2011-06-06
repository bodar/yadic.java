package com.googlecode.yadic;

import com.googlecode.yadic.resolvers.MissingResolver;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class BaseTypeMapTest {
    @Test(expected = ContainerException.class)
    public void canNeverAddAnObjectType() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(Object.class, Object.class);
    }

    @Test(expected = ContainerException.class)
    public void canNeverResolveAnObjectType() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.resolve(Object.class);
    }

    @Test
    public void ifClassImplementsClosableThenClosingTheTypeMapWillCloseTheObject() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(SomeClosableClass.class, SomeClosableClass.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    @Test
    public void removingTheTypeAlsoStopsInstanceBeingClosed() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(SomeClosableClass.class, SomeClosableClass.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        typeMap.remove(SomeClosableClass.class);
        typeMap.close();
        assertThat(closable.closed, is(false));
    }

    @Test
    public void doesNotResolveAResolverWhenClosing() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        CustomResolver resolver = new CustomResolver();
        typeMap.add(SomeClosableClass.class, resolver);
        assertThat(resolver.resolved(), is(false));
        typeMap.close();
        assertThat(resolver.resolved(), is(false));
    }

    @Test
    public void doesNotCloseAResolverIfTheResolverFailedToBeCreated() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(SomeClosableClass.class, UnsatisfiableResolver.class);
        try {
            typeMap.resolve(SomeClosableClass.class);
        } catch (ContainerException e) {
            // ignore
        }
        typeMap.close();
    }

    @Test
    public void canUseCustomResolverAndStillSupportClosingResource() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(SomeClosableClass.class, new CustomResolver());
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    @Test
    public void canUseCustomResolverAndStillSupportClosingResourceEvenWhenActivatorNeedsToBeInstantiated() throws Exception {
        TypeMap typeMap = new BaseTypeMap(new MissingResolver());
        typeMap.add(SomeClosableClass.class, CustomResolver.class);
        SomeClosableClass closable = (SomeClosableClass) typeMap.resolve(SomeClosableClass.class);
        assertThat(closable.closed, is(false));
        typeMap.close();
        assertThat(closable.closed, is(true));
    }

    public static class SomeClosableClass implements Closeable {
        public boolean closed = false;

        public void close() throws IOException {
            closed = true;
        }
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

}
