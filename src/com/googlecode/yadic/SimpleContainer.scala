package com.googlecode.yadic

import java.lang.Class
import java.util.HashMap

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {
  def this() = this ((aClass: Class[_]) => {throw new ContainerException(aClass.getName + " not found in container")})

  def this(resolver: Resolver) = this ((aCLass: Class[_]) => resolver.resolve(aCLass))

  val activators = new HashMap[Class[_], Activator[_]]

  def resolve(aClass: Class[_]): Object =
    activators.get(aClass) match {
      case null => missingHandler(aClass)
      case activator: Activator[_] => activator.activate().asInstanceOf[Object]
    }

  def resolveType[Type](aClass: Class[Type]): Type = resolve(aClass).asInstanceOf[Type]

  def add[Concrete](concrete: Class[Concrete]): Container = add(concrete, () => createInstance(concrete))

  def add[Interface, Concrete <: Interface](interface: Class[Interface], concrete: Class[Concrete]): Container = add(interface, () => createInstance(concrete))

  def addInstance(instance: Object): Container = add(instance.getClass.asInstanceOf[Class[Object]], () => instance)

  def addInstance[Interface, Concrete <: Interface](anInterface: Class[Interface], instance: Concrete) = add(anInterface, () => instance)

  def addActivator[Type](aClass: Class[Type], activator: Activator[Type]): Container = add(aClass, () => activator.activate())

  def addActivator[Type, AnActivator <: Activator[Type]](aClass: Class[Type], activator: Class[AnActivator]): Container = add(activator).add(aClass, () => resolveType(activator).activate)

  def add[Type](aClass: Class[Type], activator: () => Type): Container = {
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, new LazyActivator[Type](activator))
    }
    this
  }

  def decorate[I, C <: I](interface: Class[I], concrete: Class[C]): Container = {
    val existing = activators.get(interface)
    activators.put(interface, new LazyActivator[I](() => createInstance(concrete, (aClass: Class[_]) => {
      if (aClass.equals(interface)) existing.activate() else resolveType(aClass)
    })))
    this
  }

  def remove[T](aClass: Class[T]): Activator[T] = activators.remove(aClass).asInstanceOf[Activator[T]]

  def contains[T](aClass: Class[T]): Boolean = activators.containsKey(aClass)

  def createInstance[C](concrete: Class[C]): C = createInstance(concrete, resolve)

  def createInstance[C](concrete: Class[C], resolver: (Class[_]) => Any): C = {
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