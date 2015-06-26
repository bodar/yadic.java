package com.googlecode.yadic;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.functions.CountCalls0;
import com.googlecode.yadic.examples.ChildNode;
import com.googlecode.yadic.examples.DecorateNodeActivator;
import com.googlecode.yadic.examples.DecoratedNode;
import com.googlecode.yadic.examples.DecoratedNodeWithAdditionalArguments;
import com.googlecode.yadic.examples.GrandChildNode;
import com.googlecode.yadic.examples.Node;
import com.googlecode.yadic.examples.NodeActivator;
import com.googlecode.yadic.examples.NodeResolver;
import com.googlecode.yadic.examples.RootNode;
import com.googlecode.yadic.generics.TypeFor;
import org.junit.Test;

import java.util.List;

import static com.googlecode.totallylazy.Arrays.list;
import static com.googlecode.totallylazy.Callers.callConcurrently;
import static com.googlecode.totallylazy.functions.Sleepy0.sleepy;
import static com.googlecode.yadic.resolvers.Resolvers.asCallable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimpleContainerTest {
    @Test
    public void allowsRegisteringAClassAgainstMultipleTypes() throws Exception {
        Container container = new SimpleContainer();
        container.add(RootNode.class);
        container.addActivator(Node.class, container.getActivator(RootNode.class));

        final RootNode rootNode = container.get(RootNode.class);
        final Node node = container.get(Node.class);

        assertSame(rootNode, node);
    }

    @Test
    public void shouldResolveUsingConstructorWithMostParameters() {
        Container container = new SimpleContainer();
        container.add(ChildNode.class);
        container.add(RootNode.class);

        ChildNode childNode = container.get(ChildNode.class);

        assertThat(childNode.parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldRecursivelyResolveWithDependenciesInAnyOrder() {
        Container container = new SimpleContainer();
        container.add(ChildNode.class);
        container.add(GrandChildNode.class);
        container.add(RootNode.class);

        GrandChildNode grandChild = container.get(GrandChildNode.class);

        assertThat("1st level Dependency was not fulfilled", grandChild.parent(), is(instanceOf(ChildNode.class)));
        assertThat("2nd level Dependency was not fulfilled", grandChild.parent().parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void resolveShouldReturnSameInstanceWhenCalledTwice() {
        Container container = new SimpleContainer();
        container.add(RootNode.class);

        RootNode result1 = container.get(RootNode.class);
        RootNode result2 = container.get(RootNode.class);

        assertSame(result1, result2);
    }

    @Test
    public void shouldBeAbleToRegisterACallableClassAsAnActivator() {
        Container container = new SimpleContainer();
        container.addActivator(Node.class, NodeActivator.class);
        assertThat(container.get(Node.class), is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldBeAbleToRegisterAResolverClassAsAnActivator() {
        Container container = new SimpleContainer();
        container.addType(Node.class, NodeResolver.class);
        assertThat(container.get(Node.class), is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldOnlyCallActivatorOnce() {
        final int[] count = {0};
        Container container = new SimpleContainer();

        container.addActivator(Node.class, () -> {
            count[0]++;
            return new RootNode();

        });

        container.get(Node.class);
        container.get(Node.class);

        assertThat(count[0], is(equalTo(1)));
    }

    @Test
    public void shouldOnlyCallActivatorOnceEvenFromDifferentThreads() throws InterruptedException {
        Container container = new SimpleContainer();

        final int[] count = {0};
        container.addActivator(Node.class, sleepy(() -> {
            count[0]++;
            return new RootNode();
        }, 10));

        Sequence<Object> results = callConcurrently(asCallable(container, Node.class), asCallable(container, Node.class));

        assertSame(results.first(), results.second());
        assertThat(count[0], is(1));
    }

    @Test
    public void shouldAddAndResolveByClass() {
        Container container = new SimpleContainer();
        container.add(RootNode.class);

        RootNode result = container.get(RootNode.class);

        assertThat(result, is(not(nullValue())));
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfTypeNotInContainer() {
        Container container = new SimpleContainer();
        container.get(GrandChildNode.class);
    }

    @Test(expected = ContainerException.class)
    public void shouldThrowExceptionIfAddSameTypeTwice() {
        Container container = new SimpleContainer();
        container.add(GrandChildNode.class);
        container.add(GrandChildNode.class);
    }

    @Test
    public void shouldAddAndResolveByInterface() {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);

        Node node = container.get(Node.class);

        assertThat(node, is(instanceOf(RootNode.class)));
    }


    @Test
    public void supportsReplacingAnExistingComponent() {
        Container container = new SimpleContainer();
        container.add(Node.class, ChildNode.class);
        container.replace(Node.class, RootNode.class);

        Node node = container.get(Node.class);

        assertThat(node, is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldBeAbleToResolveAdditionalArgumentsWhenDecoratingAnExistingComponent() {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);
        container.decorate(Node.class, DecoratedNodeWithAdditionalArguments.class);
        container.addInstance(String.class, "myString");

        Node node = container.get(Node.class);

        assertThat(node, is(instanceOf(DecoratedNodeWithAdditionalArguments.class)));
        assertThat(node.parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldDecorateAnExistingComponent() {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);
        container.decorate(Node.class, DecoratedNode.class);

        Node node = container.get(Node.class);

        assertThat(node, is(instanceOf(DecoratedNode.class)));
        assertThat(node.parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldDecorateUsingActivator() throws Exception {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);
        Containers.decorateUsingActivator(container, Node.class, DecorateNodeActivator.class);

        Node node = container.get(Node.class);

        assertThat(node, is(instanceOf(DecoratedNode.class)));
        assertThat(node.parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldCallParentResolverWhenItemNotFound() {
        final boolean[] wasCalled = {false};
        Container container = new SimpleContainer(type -> {
            wasCalled[0] = true;
            return null;

        });
        container.get(Node.class);

        assertTrue(wasCalled[0]);
    }

    @Test
    public void shouldResolveByType() {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);

        Node node = container.get(Node.class);

        assertThat(node, is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldChainContainersThroughMissingAction() {
        Container parent = new SimpleContainer();
        parent.add(Node.class, RootNode.class);

        Container child = SimpleContainer.container(parent);

        Node node = child.get(Node.class);

        assertThat(node, is(instanceOf(RootNode.class)));
    }

    @Test
    public void shouldResolveUsingConstructorWithMostDependenciesThatIsSatisfiable() {
        Container container = new SimpleContainer();
        container.add(ChildNode.class);

        ChildNode node = container.get(ChildNode.class);

        assertThat(node.parent(), is(nullValue()));
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfActivatorBlowsUp() throws Exception {
        Container container = new SimpleContainer();
        container.addActivator(GrandChildNode.class, () -> {
            throw new Exception();
        });
        container.resolve(GrandChildNode.class);
        fail("should have thrown exception");
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfConstructorIsNotSatisfiable() throws Exception {
        Container container = new SimpleContainer();
        container.add(GrandChildNode.class);
        container.resolve(GrandChildNode.class);
        fail("should have thrown exception");
    }

    @Test
    public void shouldBeAbleToRemove() {
        Container container = new SimpleContainer();
        container.add(GrandChildNode.class);
        container.remove(GrandChildNode.class);
        container.add(GrandChildNode.class);
    }

    @Test
    public void shouldBeAbleToDetectExisting() {
        Container container = new SimpleContainer();
        container.add(GrandChildNode.class);
        assertThat(container.contains(GrandChildNode.class), is(true));
        container.remove(GrandChildNode.class);
        assertThat(container.contains(GrandChildNode.class), is(false));
    }

    @Test
    public void canAddObjectInstanceWithSpecificInterface() {
        Container container = new SimpleContainer();
        Node instance = new RootNode();
        container.addInstance(Node.class, instance);
        assertThat(container.get(Node.class), is(instance));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void exceptionCapturesDependencyExceptions() throws Exception {
        Container container = new SimpleContainer();
        container.add(GrandChildNode.class);
        container.add(ChildNode.class);
        try {
            container.resolve(GrandChildNode.class);
        } catch (ContainerException e) {
            assertNotNull(e.getCause());
            assertThat(e.getCauses().get(0), is(e.getCause()));
        }
    }

    @Test(expected = ContainerException.class)
    public void shouldThrowWhenClassDoesNotHaveAPublicConstructorOrStaticMethod() {
        Container container = new SimpleContainer();
        container.add(PrivateClass.class);
        container.get(PrivateClass.class);
    }

    @Test
    public void shouldSupportUserDefinedResolver() {
        final int[] count = {0};
        Container container = new SimpleContainer(type -> {
            count[0]++;
            return new RootNode();
        });
        container.add(ChildNode.class);
        assertNotNull(container.get(ChildNode.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldSupportDifferentCallables() {
        Container container = new SimpleContainer();
        CountCalls0<Integer> count = CountCalls0.counting();
        container.addActivator(Integer.class, count);
        assertNotNull(container.get(Integer.class));
        assertEquals(1, count.count());
    }

    @Test
    public void shouldBeAbleToGetTheCallableForAType() throws Exception {
        Container container = new SimpleContainer();
        container.add(RootNode.class);
        assertNotNull(container.getActivator(RootNode.class));
    }

    static private class PrivateClass {
        private PrivateClass() {
        }
    }

    @Test(expected = ContainerException.class)
    public void shouldNotBeAbleToSelectConstructorsWithMultipleParametersOfSameType() throws Exception {
        Container container = new SimpleContainer();
        container.add(ManyStringsClass.class);
        container.add(String.class);
        container.get(ManyStringsClass.class);
    }

    public static class ManyStringsClass {
        @SuppressWarnings("unused")
        public ManyStringsClass(String a, String b) {
            throw new AssertionError("Should not be called");
        }
    }

    @Test
    public void canStillConstructClassWithManyMatchingTypeConstructorByOtherConstructor() {
        Container container = new SimpleContainer();
        container.add(ManyStringsClass2.class);
        container.add(String.class);
        container.get(ManyStringsClass2.class);
    }

    @SuppressWarnings("unused")
    public static class ManyStringsClass2 {
        public ManyStringsClass2(String a, String b) {
            throw new AssertionError("Should not be called");
        }

        public ManyStringsClass2(String a) {
        }
    }

    @Test
    public void canStillConstructClassWithManyMatchingGenericTypeConstructors() {
        Container container = new SimpleContainer();
        container.add(ManyStringsClass3.class);
        container.addType(new TypeFor<List<String>>() {
        }.get(), type -> {
            return list("hello", "world");
        });
        container.addType(new TypeFor<List<Integer>>() {
        }.get(), type -> {
            return list(666, 69);
        });
        container.get(ManyStringsClass3.class);
    }

    @SuppressWarnings("unused")
    public static class ManyStringsClass3 {
        public ManyStringsClass3(List<String> a, List<Integer> b) {
        }

        public ManyStringsClass3() {
            throw new AssertionError("Should not be called");
        }
    }
}