package com.googlecode.yadic.collections;

import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.examples.*;
import com.googlecode.yadic.generics.TypeFor;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.collections.PersistentList.constructors.list;
import static com.googlecode.yadic.collections.Activator.concreate;
import static com.googlecode.yadic.collections.Activator.instance;
import static com.googlecode.yadic.collections.ListResolver.listResolver;

public class ListResolverTest {
    @Test
    public void supportsCreatingAClassByConstructors() throws Exception {
        Resolver<?> resolver = listResolver(concreate(RootNode.class));
        Object instance = resolver.resolve(RootNode.class);
        assertThat(instance, instanceOf(RootNode.class));
    }

    @Test
    public void supportsDependencies() throws Exception {
        Resolver<?> resolver = listResolver(concreate(RootNode.class), concreate(ChildNode.class));
        Object instance = resolver.resolve(ChildNode.class);
        assertThat(instance, instanceOf(ChildNode.class));
    }

    @Test
    public void onlyCreatesObjectsOnce() throws Exception {
        Resolver<?> resolver = listResolver(concreate(RootNode.class), concreate(ChildNode.class));
        ChildNode instance = (ChildNode) resolver.resolve(ChildNode.class);
        assertThat(resolver.resolve(ChildNode.class), sameInstance(instance));
        assertThat(resolver.resolve(RootNode.class), sameInstance(instance.parent()));
    }

    @Test
    public void supportsClosingClass() throws Exception {
        ListResolver resolver = listResolver(concreate(SomeClosableClass.class));
        SomeClosableClass instance = (SomeClosableClass) resolver.resolve(SomeClosableClass.class);
        assertThat(instance.closed, is(false));
        resolver.close();
        assertThat(instance.closed, is(true));
    }

    @Test(expected = ContainerException.class)
    public void throwsExceptionIfNotFound() throws Exception {
        listResolver(list()).resolve(SomeClosableClass.class);
    }

    @Test
    public void supportsRegisteringAgainstInterfaces() throws Exception {
        ListResolver resolver = listResolver(list(concreate(RootNode.class).interfaces(Node.class)));
        Node instance = (Node) resolver.resolve(Node.class);
        assertThat(instance, instanceOf(RootNode.class));
    }

    @Test
    public void supportsDecoration() throws Exception {
        PersistentList<Activator<?>> original = list(concreate(RootNode.class).interfaces(Node.class));
        ListResolver resolver = listResolver(concreate(DecoratedNode.class).decorate(Node.class, original));
        DecoratedNode instance = (DecoratedNode) resolver.resolve(Node.class);
        assertThat(instance.parent(), instanceOf(RootNode.class));
    }

    @Test
    public void supportsCustomConstruction() throws Exception {
        RootNode instance = new RootNode();
        ListResolver resolver = listResolver(list(concreate(RootNode.class).constructor(list -> instance)));
        assertThat(resolver.resolve(RootNode.class), sameInstance(instance));
    }

    @Test
    public void supportsInstance() throws Exception {
        RootNode rootNode = new RootNode();
        ListResolver resolver = listResolver(list(instance(rootNode)));
        assertThat(resolver.resolve(RootNode.class), sameInstance(rootNode));
    }

    @Test
    public void supportsInstanceWithSpecificInterface() throws Exception {
        RootNode rootNode = new RootNode();
        ListResolver resolver = listResolver(list(instance(rootNode).interfaces(Node.class)));
        assertThat(resolver.resolve(Node.class), sameInstance(rootNode));
    }

    @Test
    public void supportsCustomDestruction() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        ListResolver resolver = listResolver(list(concreate(SomeClosableClass.class).destructor(instance -> called.set(true))));
        SomeClosableClass instance = (SomeClosableClass) resolver.resolve(SomeClosableClass.class);
        assertThat(called.get(), is(false));
        assertThat(instance.closed, is(false));
        resolver.close();
        assertThat(called.get(), is(true));
        assertThat(instance.closed, is(false));
    }

    @Test
    public void supportsGenerics() throws Exception {
        Resolver resolver = listResolver(list(
                instance("bob"),
                instance(1),
                concreate(GenericType.class).types(new TypeFor<GenericType<Integer>>() { }),
                concreate(UsesGenericType.class)
        ));
        UsesGenericType genericType = (UsesGenericType) resolver.resolve(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }


}
