package com.googlecode.yadic

import java.lang.Class
import java.util.HashMap
import java.util.concurrent.Callable

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {
  def this() = this ((aClass: Class[_]) => {throw new ContainerException(aClass.getName + " not found in container")})

  def this(resolver: Resolver) = this ((aCLass: Class[_]) => resolver.resolve(aCLass))

  val callables = new HashMap[Class[_], Callable[_]]

  def resolve(aClass: Class[_]): Object =
    callables.get(aClass) match {
      case null => missingHandler(aClass)
      case callable: Callable[_] => callable.call().asInstanceOf[Object]
    }

  def get[Type](aClass: Class[Type]): Type = resolve(aClass).asInstanceOf[Type]

  def getCallable[Type](aClass: Class[Type]): Callable[Type] = callables.get(aClass).asInstanceOf[Callable[Type]]

  def add[Concrete](concrete: Class[Concrete]): Container = add(concrete, () => create(concrete))

  def add[Interface, Concrete <: Interface](interface: Class[Interface], concrete: Class[Concrete]): Container = add(interface, () => create(concrete))

  def addInstance(instance: Object): Container = add(instance.getClass.asInstanceOf[Class[Object]], () => instance)

  def addInstance[Interface, Concrete <: Interface](anInterface: Class[Interface], instance: Concrete) = add(anInterface, () => instance)

  def addCallable[Type, ExtendsType <: Type](aClass: Class[Type], callable: Callable[ExtendsType]): Container = add(aClass, () => callable.call())

  def addCallable[Type, ACallable <: Callable[Type]](aClass: Class[Type], callable: Class[ACallable]): Container = add(callable).add(aClass, () => get(callable).call)

  def add[Type](aClass: Class[Type], callable: () => Type): Container = {
    callables.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => callables.put(aClass, new LazyCallable[Type](callable))
    }
    this
  }

  def decorate[I, C <: I](interface: Class[I], concrete: Class[C]): Container = {
    val existing = callables.get(interface)
    callables.put(interface, new LazyCallable[I](() => create(concrete, (aClass: Class[_]) => {
      if (aClass.equals(interface)) existing.call() else get(aClass)
    })))
    this
  }

  def remove[T](aClass: Class[T]): Callable[T] = callables.remove(aClass).asInstanceOf[Callable[T]]

  def contains[T](aClass: Class[T]): Boolean = callables.containsKey(aClass)

  def create[C](concrete: Class[C]): C = create(concrete, resolve)

  def create[C](concrete: Class[C], resolver: (Class[_]) => Any): C = {
    val constructors = concrete.getConstructors.toList.sort(_.getParameterTypes.length > _.getParameterTypes.length)
    constructors.foreach(constructor => {
      try {
        val instances = constructor.getParameterTypes.map(resolver(_).asInstanceOf[Object])
        return constructor.newInstance(instances: _*).asInstanceOf[C]
      } catch {
        case e: ContainerException =>
      }
    })
    throw new ContainerException(concrete.getName + " does not have a satisfiable constructor")
  }
}