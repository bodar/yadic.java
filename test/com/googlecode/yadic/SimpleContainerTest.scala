package com.googlecode.yadic

import org.hamcrest.CoreMatchers._
import org.junit.Assert._
import org.junit.{Test}
import com.googlecode.yadic.SimpleContainerTest._
import java.util.ArrayList
import java.util.List
import java.util.concurrent.{TimeUnit, Future, Executors, Callable}

class SimpleContainerTest {
  @Test
  def exceptionCapturesDependencyExceptions {
    val container = new SimpleContainer
    container.add(classOf[DependsOnMyThing])
    container.add(classOf[MyThing])
    try {
      container.resolve(classOf[DependsOnMyThing])
    } catch {
      case e:ContainerException => {
        assertNotNull(e.getCause)
        assertThat(e.getCauses.get(0), is(e.getCause) )
      }
    }
  }

  @Test
  def canAddObjectInstanceWithSpecificInterface {
    val container = new SimpleContainer
    val instance:Thing = new ThingWithNoDependencies
    container.addInstance(classOf[Thing], instance)

    assertThat(container.get(classOf[Thing]), is(instance))
  }

  @Test
  def canAddObjectInstance {
    val container = new SimpleContainer
    val instance:Object = new ThingWithNoDependencies
    container.addInstance(instance)

    assertThat(container.get(classOf[ThingWithNoDependencies]), is(instance))
  }

  @Test
  def shouldBeAbleToAddACallableClass {
    val container = new SimpleContainer
    container.addActivator(classOf[MyThing], classOf[MyThingActivator])
    val thing = container.get(classOf[MyThing])
    assertThat(thing.dependency, is(nullValue(classOf[MyDependency])))
  }

  @Test
  def shouldBeAbleToDetectExisting {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    assertThat(container.contains(classOf[MyThing]), is(true))
    container.remove(classOf[MyThing])
    assertThat(container.contains(classOf[MyThing]), is(false))
  }

  @Test
  def shouldBeAbleToRemove {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    val activator = container.remove(classOf[MyThing])
    container.add(classOf[MyThing])
  }

  @Test {val expected = classOf[ContainerException]}
  def resolveShouldThrowExceptionIfConstructorIsNotSatifiable {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    container.resolve(classOf[MyThing])
    fail("should have thrown exception")
  }

  @Test
  def shouldOnlyCallCreationLambdaOnceEvenFromDifferentThreads {
    var count = 0
    val container = new SimpleContainer

    container.add(classOf[Thing], () => {
      count = count + 1
      Thread.sleep(10)
      new ThingWithNoDependencies
    })

    val service = Executors.newFixedThreadPool(2)

    val collection = new ArrayList[Callable[Thing]]
    collection.add(new Creator(container))
    collection.add(new Creator(container))
    val results: List[Future[Thing]] = service.invokeAll(collection)
    service.shutdown
    service.awaitTermination(50, TimeUnit.MILLISECONDS)

    assertThat(count, is(1))
    assertSame(results.get(0).get, results.get(1).get)
  }

  @Test
  def shouldResolveUsingConstructorWithMostDependenciesThatIsSatisfiable {
    val container = new SimpleContainer
    container.add(classOf[MyThingWithReverseConstructor])

    var myThing: MyThingWithReverseConstructor = container.get(classOf[MyThingWithReverseConstructor])

    assertThat(myThing.dependency, is(nullValue(classOf[Thing])))
  }

  @Test
  def shouldChainContainersThroughMissingAction {
    val parent = new SimpleContainer
    parent.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val child = new SimpleContainer(parent)

    val thing = child.get(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldResolveByType {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val thing = container.get(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldCallMissingMethodWhenItemNotFound {
    var wasCalled = false
    val container = new SimpleContainer((_) =>
      {
        wasCalled = true
        null
      })
    container.get(classOf[Thing])

    assertTrue(wasCalled)
  }

  @Test
  def shouldOnlyCallCreationLambdaOnce {
    var count = 0
    val container = new SimpleContainer

    container.add(classOf[Thing], () => {
      count = count + 1
      new ThingWithNoDependencies
    })

    container.get(classOf[Thing])
    val thing = container.get(classOf[Thing])

    assertThat(count, is(equalTo(1)))
  }

  @Test
  def shouldDecorateAnExistingComponent {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])
    container.decorate(classOf[Thing], classOf[DecoratedThing])

    var thing = container.get(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[DecoratedThing])))
    assertThat(thing.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldAddAndResolveByConcrete {
    val container = new SimpleContainer
    container.add(classOf[Thing], () => new ThingWithNoDependencies)

    var thing = container.get(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldAddAndResolveByInterface {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])

    var thing = container.get(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test {val expected = classOf[ContainerException]}
  def shouldThrowExceptionIfAddSameTypeTwice {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    container.add(classOf[MyThing])
    fail("should have thrown exception")
  }

  @Test {val expected = classOf[ContainerException]}
  def resolveShouldThrowExceptionIfTypeNotInContainer {
    val container = new SimpleContainer
    container.get(classOf[MyThing])
    fail("should have thrown exception")
  }

  @Test
  def shouldAddAndResolveByClass {
    val container = new SimpleContainer
    container.add(classOf[ThingWithNoDependencies])

    var result = container.get(classOf[ThingWithNoDependencies])

    assertThat(result, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def resolveShouldReturnSameInstanceWhenCalledTwice {
    val container = new SimpleContainer
    container.add(classOf[ThingWithNoDependencies])

    var result1 = container.get(classOf[ThingWithNoDependencies])
    var result2 = container.get(classOf[ThingWithNoDependencies])

    assertSame(result1, result2)
  }

  @Test
  def shouldResolveDependencies {
    val container = new SimpleContainer
    container.add(classOf[MyDependency])
    container.add(classOf[ThingWithNoDependencies])

    var myThing = container.get(classOf[MyDependency])

    assertThat(myThing.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldRecursivelyResolveDependencies {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    container.add(classOf[MyDependency])
    container.add(classOf[ThingWithNoDependencies])

    var myThing = container.get(classOf[MyThing])

    assertThat(myThing.dependency, is(instanceOf(classOf[MyDependency])))
    assertThat(myThing.dependency.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldResolveWithDependenciesInAnyOrder {
    val container = new SimpleContainer
    container.add(classOf[MyDependency])
    container.add(classOf[MyThing])
    container.add(classOf[ThingWithNoDependencies])

    var myThing = container.get(classOf[MyThing])

    assertThat("1st level Dependency was not fulfilled", myThing.dependency, is(instanceOf(classOf[MyDependency])))
    assertThat("2nd level Dependency was not fulfiled", myThing.dependency.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldResolveUsingConstructorWithMostDependencies {
    val container = new SimpleContainer
    container.add(classOf[MyThingWithReverseConstructor])
    container.add(classOf[ThingWithNoDependencies])

    var myThing: MyThingWithReverseConstructor = container.get(classOf[MyThingWithReverseConstructor])

    assertThat("Wrong constructor was used", myThing.dependency, is(notNullValue(classOf[Thing])))
    assertThat(myThing.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }
}

object SimpleContainerTest {
  class MyThingActivator extends Callable[MyThing]{
    def call = new MyThing(null)
  }

  class Creator(container: SimpleContainer) extends Callable[Thing] {
    def call = container.get(classOf[Thing])
  }

  class MyThingWithReverseConstructor(val dependency: ThingWithNoDependencies) extends Thing {
    def this() = this (null)
  }

  class DependsOnMyThing(val dependency: MyThing) extends Thing

  class MyThing(val dependency: MyDependency) extends Thing

  class MyDependency(val dependency: ThingWithNoDependencies) extends Thing

  class ThingWithNoDependencies extends Thing {
    val dependency: Thing = null
  }

  class DecoratedThing(val dependency: Thing) extends Thing

  trait Thing {
    val dependency: Thing
  }
}