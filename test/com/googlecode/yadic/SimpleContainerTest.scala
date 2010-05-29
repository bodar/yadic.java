package com.googlecode.yadic

import org.hamcrest.CoreMatchers._
import org.junit.Assert.{assertThat, assertTrue, fail, assertSame}
import org.junit.{Test}
import com.googlecode.yadic.SimpleContainerTest._
import java.util.ArrayList
import java.util.List
import java.util.concurrent.{TimeUnit, Future, Executors, Callable}

class SimpleContainerTest {
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
    val results:List[Future[Thing]] = service.invokeAll(collection)
    service.shutdown
    service.awaitTermination(50, TimeUnit.MILLISECONDS)

    assertThat( count, is(1) )
    assertSame( results.get(0).get, results.get(1).get)
  }

  @Test
  def shouldResolveUsingConstructorWithMostDependenciesThatIsSatisfiable {
    val container = new SimpleContainer
    container.add(classOf[MyThingWithReverseConstructor])

    var myThing: MyThingWithReverseConstructor = container.resolveType(classOf[MyThingWithReverseConstructor])

    assertThat(myThing.dependency, is(nullValue(classOf[Thing])))
  }

  @Test
  def shouldChainContainersThroughMissingAction {
    val parent = new SimpleContainer
    parent.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val child = new SimpleContainer(parent)

    val thing = child.resolveType(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldResolveByType {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val thing = container.resolveType(classOf[Thing])

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
    container.resolveType(classOf[Thing])

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

    container.resolveType(classOf[Thing])
    val thing = container.resolveType(classOf[Thing])

    assertThat(count, is(equalTo(1)))
  }

  @Test
  def shouldDecorateAnExistingComponent {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])
    container.decorate(classOf[Thing], classOf[DecoratedThing])

    var thing = container.resolveType(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[DecoratedThing])))
    assertThat(thing.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def canAddObjectInstance {
    val container = new SimpleContainer
    val instance = new ThingWithNoDependencies
    container.add(instance)

    assertThat(container.resolveType(classOf[ThingWithNoDependencies]), is(instance))
  }

  @Test
  def shouldAddAndReolveByConcrete {
    val container = new SimpleContainer
    container.add(classOf[Thing], () => new ThingWithNoDependencies)

    var thing = container.resolveType(classOf[Thing])

    assertThat(thing, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldAddAndResolveByInterface {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])

    var thing = container.resolveType(classOf[Thing])

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
    container.resolveType(classOf[MyThing])
    fail("should have thrown exception")
  }

  @Test
  def shouldAddAndResolveByClass {
    val container = new SimpleContainer
    container.add(classOf[ThingWithNoDependencies])

    var result = container.resolveType(classOf[ThingWithNoDependencies])

    assertThat(result, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def resolveShouldReturnSameInstanceWhenCalledTwice {
    val container = new SimpleContainer
    container.add(classOf[ThingWithNoDependencies])

    var result1 = container.resolveType(classOf[ThingWithNoDependencies])
    var result2 = container.resolveType(classOf[ThingWithNoDependencies])

    assertSame(result1, result2)
  }

  @Test
  def shouldResolveDependencies {
    val container = new SimpleContainer
    container.add(classOf[MyDependency])
    container.add(classOf[ThingWithNoDependencies])

    var myThing = container.resolveType(classOf[MyDependency])

    assertThat(myThing.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldRecursivelyResolveDependencies {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    container.add(classOf[MyDependency])
    container.add(classOf[ThingWithNoDependencies])

    var myThing = container.resolveType(classOf[MyThing])

    assertThat(myThing.dependency, is(instanceOf(classOf[MyDependency])))
    assertThat(myThing.dependency.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldResolveWithDependenciesInAnyOrder {
    val container = new SimpleContainer
    container.add(classOf[MyDependency])
    container.add(classOf[MyThing])
    container.add(classOf[ThingWithNoDependencies])

    var myThing = container.resolveType(classOf[MyThing])

    assertThat("1st level Dependency was not fulfilled", myThing.dependency, is(instanceOf(classOf[MyDependency])))
    assertThat("2nd level Dependency was not fulfiled", myThing.dependency.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }

  @Test
  def shouldResolveUsingConstructorWithMostDependencies {
    val container = new SimpleContainer
    container.add(classOf[MyThingWithReverseConstructor])
    container.add(classOf[ThingWithNoDependencies])

    var myThing: MyThingWithReverseConstructor = container.resolveType(classOf[MyThingWithReverseConstructor])

    assertThat("Wrong constructor was used", myThing.dependency, is(notNullValue(classOf[Thing])))
    assertThat(myThing.dependency, is(instanceOf(classOf[ThingWithNoDependencies])))
  }
}

object SimpleContainerTest {
  class Creator(container:SimpleContainer) extends Callable[Thing] {
    def call = container.resolveType(classOf[Thing])
  }

  class MyThingWithReverseConstructor(val dependency: ThingWithNoDependencies) extends Thing {
    def this() = this (null)
  }

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