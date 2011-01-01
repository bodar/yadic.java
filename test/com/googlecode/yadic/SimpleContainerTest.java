package com.googlecode.yadic;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.examples.*;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

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
        container.add(MemoryUserRepository.class);
        container.addActivator(UserRepository.class, container.getActivator(MemoryUserRepository.class));

        final MemoryUserRepository memoryUserRepository = container.get(MemoryUserRepository.class);
        final UserRepository userRepository = container.get(UserRepository.class);

        assertSame(memoryUserRepository, userRepository);
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
    public void shouldOnlyCallCreationLambdaOnce() {
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
    public void shouldOnlyCallCreationLambdaOnceEvenFromDifferentThreads() throws InterruptedException {
        Container container = new SimpleContainer();

        final int[] count = {0};
        container.addActivator(Node.class, sleepy(new Callable<Node>() {
            public Node call() throws Exception {
                count[0]++;
                return new RootNode();
            }
        }, 10));

        Sequence<Node> results = callConcurrently(new NodeActivator(container), new NodeActivator(container));

        assertSame(results.first(), results.second());
        assertThat(count[0], is(1));
    }

    @Test
    public void shouldAddAndResolveByClass() {
        Container container = new SimpleContainer();
        container.add(RootNode.class);

        RootNode result = container.get(RootNode.class);

        assertThat(result, is(not(nullValue(RootNode.class))));
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfTypeNotInContainer() {
        Container container = new SimpleContainer();
        container.get(GrandChildNode.class);
        fail("should have thrown exception");
    }

    @Test(expected = ContainerException.class)
    public void shouldThrowExceptionIfAddSameTypeTwice() {
        Container container = new SimpleContainer();
        container.add(GrandChildNode.class);
        container.add(GrandChildNode.class);
        fail("should have thrown exception");
    }

    @Test
    public void shouldAddAndResolveByInterface() {
        Container container = new SimpleContainer();
        container.add(Node.class, RootNode.class);

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
    public void supportsReplacingAnExistingComponent() {
        Container container = new SimpleContainer();
        container.add(UserRepository.class, MemoryUserRepository.class);
        container.replace(UserRepository.class, AlternativeUserRepository.class);

        UserRepository userRepository = container.get(UserRepository.class);

        assertThat(userRepository, is(instanceOf(AlternativeUserRepository.class)));
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
    public void shouldCallMissingMethodWhenItemNotFound() {
        final boolean[] wasCalled = {false};
        Container container = new SimpleContainer(new Resolver() {
            public Object resolve(Type type) {
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

        assertThat(node.parent(), is(nullValue(Node.class)));
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfActivatorBlowsUp() {
        Container container = new SimpleContainer();
        container.addActivator(GrandChildNode.class, new Callable<GrandChildNode>() {
            public GrandChildNode call() throws Exception {
                throw new Exception();
            }
        });
        container.resolve(GrandChildNode.class);
        fail("should have thrown exception");
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfConstructorIsNotSatisfiable() {
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
        container.add(DependsOnMyNode.class);
        container.add(GrandChildNode.class);
        try {
            container.resolve(DependsOnMyNode.class);
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
            public Object resolve(Type type) {
                count[0]++;
                return new Dependency();
            }
        });
        container.add(Depends.class);
        assertNotNull(container.get(Depends.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldSupportDifferentCallables() {
        Container container = new SimpleContainer();
        final int[] count = {0};
        container.addActivator(NoDependencies.class, new NoDependanciesCallable(count));
        assertNotNull(container.get(NoDependencies.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void shouldBeAbleToGetTheCallableForAType() throws Exception {
        Container container = new SimpleContainer();
        container.add(NoDependencies.class);
        assertNotNull(container.getActivator(NoDependencies.class));
    }

    @Test
    public void shouldBeAbleToReregisterAClassAgainstAParentInterface() throws Exception {
        Container container = new SimpleContainer();
        container.add(MemoryUserRepository.class);
        container.addActivator(UserRepository.class, container.getActivator(MemoryUserRepository.class));
        assertNotNull(container.getActivator(UserRepository.class));
    }


    static private class PrivateClass {
        private PrivateClass() {
        }
    }

}
