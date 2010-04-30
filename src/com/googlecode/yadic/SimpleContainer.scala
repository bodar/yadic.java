package com.googlecode.yadic

import java.lang.Class
import java.util.HashMap

class SimpleContainer(missingHandler: (Class[_]) => Any) extends Container {
  def this() = this ((aClass:Class[_]) => {throw new ContainerException(aClass.getName + " not found in container")})

  val activators = new HashMap[Class[_], Activator[_]]

  def resolve(aClass: Class[_]) = resolveType(aClass).asInstanceOf[Object]

  def resolveType[A]( aClass:Class[A] ): A = {
    activators.get(aClass) match {
      case null => missingHandler(aClass).asInstanceOf[A]
      case activator:Activator[A] => activator.activate()
    }
  }

  def add[C](concrete: Class[C]) : Unit = add(concrete, () => createInstance(concrete) )

  def add[I, C <: I](interface: Class[I], concrete: Class[C]): Unit = add(interface, () => createInstance(concrete) )

  def add[T](a: Class[T], activator: Activator[T]):Unit = add(a, () => activator.activate() )

  def add[T](aClass: Class[T], activator: () => T): Unit = {
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, new LazyActivator[T](activator))
    }
  }

  def decorate[I, C <: I](interface: Class[I], concrete: Class[C]): Unit = {
    val existing = activators.get(interface)
    activators.put(interface, new LazyActivator[I](() => createInstance(concrete, (aClass: Class[_]) => {
      if(aClass.equals(interface)) existing.activate() else resolveType(aClass)
    })))
  }

  def createInstance[T](aClass: Class[T]): T = createInstance(aClass, resolve)

  def createInstance[T](aClass: Class[T], resolver: (Class[_]) => Any ): T = {
    val constructors = aClass.getConstructors.toList.sort(_.getParameterTypes.length > _.getParameterTypes.length)
    constructors.foreach( constructor => {
      try {
        val instances = constructor.getParameterTypes.map( resolver(_).asInstanceOf[Object] )
        return constructor.newInstance(instances: _*).asInstanceOf[T]
      } catch {
        case e:ContainerException =>
      }
    })
    throw new ContainerException(aClass.getName + " does not have a satisfiable constructor")
  }
}