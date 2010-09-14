package com.googlecode.yadic

import java.lang.Class
import java.util.HashMap
import java.util.concurrent.Callable

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {
  def this() = this ((aClass: Class[_]) => {throw new ContainerException(aClass.getName + " not found in container")})

  def this(resolver: Resolver) = this ((aCLass: Class[_]) => resolver.resolve(aCLass))

  val activators = new HashMap[Class[_], Callable[_]]

  def resolve(aClass: Class[_]): Object =
    activators.get(aClass) match {
      case null => missingHandler(aClass)
      case activator: Callable[_] => activator.call().asInstanceOf[Object]
    }

  def get[Type](aClass: Class[Type]): Type = resolve(aClass).asInstanceOf[Type]

  def getActivator[Type](aClass: Class[Type]): Callable[Type] = activators.get(aClass).asInstanceOf[Callable[Type]]

  def add[Concrete](concrete: Class[Concrete]): Container = add(concrete, () => create(concrete))

  def add[Interface, Concrete <: Interface](interface: Class[Interface], concrete: Class[Concrete]): Container = add(interface, () => create(concrete))

  def addInstance(instance: Object): Container = add(instance.getClass.asInstanceOf[Class[Object]], () => instance)

  def addInstance[Interface, Concrete <: Interface](anInterface: Class[Interface], instance: Concrete) = add(anInterface, () => instance)

  def addActivator[Type, ExtendsType <: Type](aClass: Class[Type], activator: Callable[ExtendsType]): Container = add(aClass, () => activator.call())

  def addActivator[Type, AnActivator <: Callable[Type]](aClass: Class[Type], activator: Class[AnActivator]): Container = add(activator).add(aClass, () => get(activator).call)

  def add[Type](aClass: Class[Type], activator: () => Type): Container = {
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, new LazyActivator[Type](activator))
    }
    this
  }

  def decorate[I, C <: I](interface: Class[I], concrete: Class[C]): Container = {
    val existing = activators.get(interface)
    activators.put(interface, new LazyActivator[I](() => create(concrete, (aClass: Class[_]) => {
      if (aClass.equals(interface)) existing.call() else get(aClass)
    })))
    this
  }

  def remove[T](aClass: Class[T]): Callable[T] = activators.remove(aClass).asInstanceOf[Callable[T]]

  def contains[T](aClass: Class[T]): Boolean = activators.containsKey(aClass)

  def create[C](concrete: Class[C]): C = create(concrete, resolve)

  def create[C](concrete: Class[C], resolver: (Class[_]) => Any): C = {
    val constructors = concrete.getConstructors.toList.sort(_.getParameterTypes.length > _.getParameterTypes.length)
    val exceptions = constructors.map(constructor => {
      try {
        val instances = constructor.getParameterTypes.map(resolver(_).asInstanceOf[Object])
        return constructor.newInstance(instances: _*).asInstanceOf[C]
      } catch {
        case e:ContainerException => e
      }
    })
    throw new ContainerException(concrete.getName + " does not have a satisfiable constructor", exceptions)
  }
}