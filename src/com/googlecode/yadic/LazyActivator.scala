package com.googlecode.yadic

class LazyActivator(activator: () => Object) extends Activator {
  lazy val activate = activator()
}