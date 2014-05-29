package com.googlecode.yadic.collections;

import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.examples.ChildNode;
import com.googlecode.yadic.examples.RootNode;
import com.googlecode.yadic.examples.SomeClosableClass;
import org.junit.Test;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.collections.PersistentList.constructors.list;
import static com.googlecode.yadic.collections.Activator.activator;
import static com.googlecode.yadic.collections.ListResolver.listResolver;

public class ClassMapsTest {
    @Test
    public void supportsConstructionOfClass() throws Exception {
        Resolver<?> resolver = listResolver(list(activator(RootNode.class)));
        Object instance = resolver.resolve(RootNode.class);
        assertThat(instance, instanceOf(RootNode.class));
    }

    @Test
    public void supportsDependencies() throws Exception {
        Resolver<?> resolver = listResolver(list(activator(RootNode.class), activator(ChildNode.class)));
        Object instance = resolver.resolve(ChildNode.class);
        assertThat(instance, instanceOf(ChildNode.class));
    }

    @Test
    public void onlyCreatesObjectsOnce() throws Exception {
        Resolver<?> resolver = listResolver(list(activator(RootNode.class), activator(ChildNode.class)));
        ChildNode instance = (ChildNode) resolver.resolve(ChildNode.class);
        assertThat(resolver.resolve(ChildNode.class), sameInstance(instance));
        assertThat(resolver.resolve(RootNode.class), sameInstance(instance.parent()));
    }

    @Test
    public void supportsClosingClass() throws Exception {
        ListResolver resolver = listResolver(list(activator(SomeClosableClass.class)));
        SomeClosableClass instance = (SomeClosableClass) resolver.resolve(SomeClosableClass.class);
        assertThat(instance.closed, is(false));
        resolver.close();
        assertThat(instance.closed, is(true));
    }


}
