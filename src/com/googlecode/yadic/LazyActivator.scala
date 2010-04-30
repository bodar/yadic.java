package com.googlecode.yadic

class LazyActivator[T](activator: () => T) extends Activator[T] {
  lazy val activate = activator()
}