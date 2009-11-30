package com.googlecode.yadic

import java.lang.Class
import java.util.HashMap

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {
  def this() = this ((aClass:Class[_]) => {throw new ContainerException(aClass.getName + " not found in container")})

  val activators = new HashMap[Class[_], Activator]

  def resolve(aClass: Class[_]): Object = {
    activators.get(aClass) match {
      case null => missingHandler(aClass)
      case activator:Activator => activator.activate()
    }
  }

  def add[C <: Object](concrete: Class[C]): Unit = add(concrete, () => createInstance(concrete) )
            
  def add[I <: Object, C <: I](interface: Class[I], concrete: Class[C]): Unit = add(interface, () => createInstance(concrete) )

  def add[A <: Object](aClass: Class[A], activator: () => A): Unit = {
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, new LazyActivator(activator))
    }
  }

  def decorate[A <: Object, B <: A](interface: Class[A], concrete: Class[B]): Unit = {
    val existing = activators.get(interface)
    activators.put(interface, new LazyActivator(() => createInstance(concrete, (aClass: Class[_]) => {
      if(aClass.equals(interface)) existing.activate else resolve(aClass)
    })))
  }

  def createInstance[T <: Object](aClass: Class[T]): T = createInstance(aClass, resolve(_))

  def createInstance[T <: Object](aClass: Class[T], resolver: (Class[_]) => Object ): T = {
    val constructors = aClass.getConstructors.toList.sort(_.getParameterTypes.length > _.getParameterTypes.length)
    constructors.foreach( constructor => {
      try {
        val instances = constructor.getParameterTypes.map( resolver(_) )
        return constructor.newInstance(instances: _*).asInstanceOf[T]
      } catch {
        case e:ContainerException =>
      }
    })
    throw new ContainerException(aClass.getName + " does not have a satisfiable constructor")
  }
}