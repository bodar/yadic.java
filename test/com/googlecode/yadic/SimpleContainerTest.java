package com.googlecode.yadic;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.callables.CountingCallable;
import com.googlecode.yadic.examples.*;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.curry;
import static com.googlecode.totallylazy.Callers.callConcurrently;
import static com.googlecode.totallylazy.callables.SleepyCallable.sleepy;
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
    public void shouldOnlyCallActivatorOnce() {
        final int[] count = {0};
        Container container = new SimpleContainer();

        container.addActivator(Node.class, new Callable<Node>() {
            public Node call() throws Exception {
                count[0]++;
                return new RootNode();

            }
        });

        container.get(Node.class);
        container.get(Node.class);

        assertThat(count[0], is(equalTo(1)));
    }

    @Test
    public void shouldOnlyCallActivatorOnceEvenFromDifferentThreads() throws InterruptedException {
        Container container = new SimpleContainer();

        final int[] count = {0};
        container.addActivator(Node.class, sleepy(new Callable<Node>() {
            public Node call() throws Exception {
                count[0]++;
                return new RootNode();
            }
        }, 10));

        Sequence<Node> results = callConcurrently(curry(new NodeActivator(container), null), curry(new NodeActivator(container), null));

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
    public void shouldCallParentResolverWhenItemNotFound() {
        final boolean[] wasCalled = {false};
        Container container = new SimpleContainer(new Resolver() {
            public Object resolve(Type type) throws Exception {
                wasCalled[0] = true;
                return null;

            }
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

        Container child = new SimpleContainer(parent);

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
    public void getShouldThrowExceptionIfActivatorBlowsUp() throws Exception {
        Container container = new SimpleContainer();
        container.addActivator(GrandChildNode.class, new Callable<GrandChildNode>() {
            public GrandChildNode call() throws Exception {
                throw new Exception();
            }
        });
        container.get(GrandChildNode.class);
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

    @Test
    public void shouldThrowWhenClassDoesNotHaveAPublicConstructor() {
        try {
            Container container = new SimpleContainer();
            container.add(PrivateClass.class);
            container.get(PrivateClass.class);
        } catch (ContainerException e) {
            assertThat(e.getMessage(), is(PrivateClass.class.getName() + " does not have a public constructor"));
        }
    }

    @Test
    public void shouldSupportUserDefinedResolver() {
        final int[] count = {0};
        Container container = new SimpleContainer(new Resolver() {
            public Object resolve(Type type) throws Exception {
                count[0]++;
                return new RootNode();
            }
        });
        container.add(ChildNode.class);
        assertNotNull(container.get(ChildNode.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldSupportDifferentCallables() {
        Container container = new SimpleContainer();
        CountingCallable count = CountingCallable.counting();
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

}
