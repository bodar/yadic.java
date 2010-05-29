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

  def resolveType[A](aClass: Class[A]): A = resolve(aClass).asInstanceOf[A]

  def add[C](concrete: Class[C]): Container = add(concrete, () => createInstance(concrete))

  def add[I, C <: I](interface: Class[I], concrete: Class[C]): Container = add(interface, () => createInstance(concrete))

  def add[T](a: Class[T], activator: Activator[T]): Container = add(a, () => activator.activate())

  def add(instance:Object): Container = add(instance.getClass.asInstanceOf[Class[Object]], () => instance)

  def add[T](aClass: Class[T], activator: () => T): Container = {
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, new LazyActivator[T](activator))
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

  def remove[T](aClass: Class[T]):Activator[T] = activators.remove(aClass).asInstanceOf[Activator[T]]

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