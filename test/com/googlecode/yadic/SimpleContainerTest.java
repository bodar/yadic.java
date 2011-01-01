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
    public void allowsRegisteringAnObjectWithTwoInterfaces() throws Exception {
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
        container.add(MyThingWithReverseConstructor.class);
        container.add(ThingWithNoDependencies.class);

        MyThingWithReverseConstructor myThing = container.get(MyThingWithReverseConstructor.class);

        assertThat("Wrong constructor was used", myThing.dependency(), is(not(nullValue(Thing.class))));
        assertThat(myThing.dependency(), is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldResolveWithDependenciesInAnyOrder() {
        Container container = new SimpleContainer();
        container.add(MyDependency.class);
        container.add(MyThing.class);
        container.add(ThingWithNoDependencies.class);

        MyThing myThing = container.get(MyThing.class);

        assertThat("1st level Dependency was not fulfilled", myThing.dependency(), is(instanceOf(MyDependency.class)));
        assertThat("2nd level Dependency was not fulfilled", myThing.dependency().dependency(), is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldRecursivelyResolveDependencies() {
        Container container = new SimpleContainer();
        container.add(MyThing.class);
        container.add(MyDependency.class);
        container.add(ThingWithNoDependencies.class);

        MyThing myThing = container.get(MyThing.class);

        assertThat(myThing.dependency(), is(instanceOf(MyDependency.class)));
        assertThat(myThing.dependency().dependency(), is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldResolveDependencies() {
        Container container = new SimpleContainer();
        container.add(MyDependency.class);
        container.add(ThingWithNoDependencies.class);

        MyDependency myThing = container.get(MyDependency.class);

        assertThat(myThing.dependency(), is(not(nullValue(Thing.class))));
    }

    @Test
    public void resolveShouldReturnSameInstanceWhenCalledTwice() {
        Container container = new SimpleContainer();
        container.add(ThingWithNoDependencies.class);

        ThingWithNoDependencies result1 = container.get(ThingWithNoDependencies.class);
        ThingWithNoDependencies result2 = container.get(ThingWithNoDependencies.class);

        assertSame(result1, result2);
    }

    @Test
    public void shouldAddAndResolveByClass() {
        Container container = new SimpleContainer();
        container.add(ThingWithNoDependencies.class);

        ThingWithNoDependencies result = container.get(ThingWithNoDependencies.class);

        assertThat(result, is(not(nullValue(ThingWithNoDependencies.class))));
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfTypeNotInContainer() {
        Container container = new SimpleContainer();
        container.get(MyThing.class);
        fail("should have thrown exception");
    }

    @Test(expected = ContainerException.class)
    public void shouldThrowExceptionIfAddSameTypeTwice() {
        Container container = new SimpleContainer();
        container.add(MyThing.class);
        container.add(MyThing.class);
        fail("should have thrown exception");
    }

    @Test
    public void shouldAddAndResolveByInterface() {
        Container container = new SimpleContainer();
        container.add(Thing.class, ThingWithNoDependencies.class);

        Thing thing = container.get(Thing.class);

        assertThat(thing, is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldBeAbleToResolveAdditionalArgumentsWhenDecoratingAnExistingComponent() {
        Container container = new SimpleContainer();
        container.add(Thing.class, ThingWithNoDependencies.class);
        container.decorate(Thing.class, DecoratedThingWithAdditionalArguments.class);
        container.addInstance(String.class, "myString");

        Thing thing = container.get(Thing.class);

        assertThat(thing, is(instanceOf(DecoratedThingWithAdditionalArguments.class)));
        assertThat(thing.dependency(), is(instanceOf(ThingWithNoDependencies.class)));
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
        container.add(Thing.class, ThingWithNoDependencies.class);
        container.decorate(Thing.class, DecoratedThing.class);

        Thing thing = container.get(Thing.class);

        assertThat(thing, is(instanceOf(DecoratedThing.class)));
        assertThat(thing.dependency(), is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldOnlyCallCreationLambdaOnce() {
        final int[] count = {0};
        Container container = new SimpleContainer();

        container.addActivator(Thing.class, new Callable<Thing>() {
            public Thing call() throws Exception {
                count[0]++;
                return new ThingWithNoDependencies();

            }
        });

        container.get(Thing.class);
        container.get(Thing.class);

        assertThat(count[0], is(equalTo(1)));
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
        container.get(Thing.class);

        assertTrue(wasCalled[0]);
    }

    @Test
    public void shouldResolveByType() {
        Container container = new SimpleContainer();
        container.add(Thing.class, ThingWithNoDependencies.class);

        Thing thing = container.get(Thing.class);

        assertThat(thing, is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldChainContainersThroughMissingAction() {
        Container parent = new SimpleContainer();
        parent.add(Thing.class, ThingWithNoDependencies.class);

        Container child = new SimpleContainer(parent);

        Thing thing = child.get(Thing.class);

        assertThat(thing, is(instanceOf(ThingWithNoDependencies.class)));
    }

    @Test
    public void shouldResolveUsingConstructorWithMostDependenciesThatIsSatisfiable() {
        Container container = new SimpleContainer();
        container.add(MyThingWithReverseConstructor.class);

        MyThingWithReverseConstructor myThing = container.get(MyThingWithReverseConstructor.class);

        assertThat(myThing.dependency(), is(nullValue(Thing.class)));
    }

    @Test
    public void shouldOnlyCallCreationLambdaOnceEvenFromDifferentThreads() throws InterruptedException {
        Container container = new SimpleContainer();

        final int[] count = {0};
        container.addActivator(Thing.class, sleepy(new Callable<Thing>() {
            public Thing call() throws Exception {
                count[0]++;
                return new ThingWithNoDependencies();
            }
        }, 10));

        Sequence<Thing> results = callConcurrently(new Creator(container), new Creator(container));

        assertSame(results.first(), results.second());
        assertThat(count[0], is(1));
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfActivatorBlowsUp() {
        Container container = new SimpleContainer();
        container.addActivator(MyThing.class, new Callable<MyThing>() {
            public MyThing call() throws Exception {
                throw new Exception();
            }
        });
        container.resolve(MyThing.class);
        fail("should have thrown exception");
    }

    @Test(expected = ContainerException.class)
    public void resolveShouldThrowExceptionIfConstructorIsNotSatisfiable() {
        Container container = new SimpleContainer();
        container.add(MyThing.class);
        container.resolve(MyThing.class);
        fail("should have thrown exception");
    }

    @Test
    public void shouldBeAbleToRemove() {
        Container container = new SimpleContainer();
        container.add(MyThing.class);
        container.remove(MyThing.class);
        container.add(MyThing.class);
    }

    @Test
    public void shouldBeAbleToDetectExisting() {
        Container container = new SimpleContainer();
        container.add(MyThing.class);
        assertThat(container.contains(MyThing.class), is(true));
        container.remove(MyThing.class);
        assertThat(container.contains(MyThing.class), is(false));
    }

    @Test
    public void canAddObjectInstanceWithSpecificInterface() {
        Container container = new SimpleContainer();
        Thing instance = new ThingWithNoDependencies();
        container.addInstance(Thing.class, instance);
        assertThat(container.get(Thing.class), is(instance));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void exceptionCapturesDependencyExceptions() throws Exception {
        Container container = new SimpleContainer();
        container.add(DependsOnMyThing.class);
        container.add(MyThing.class);
        try {
            container.resolve(DependsOnMyThing.class);
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
