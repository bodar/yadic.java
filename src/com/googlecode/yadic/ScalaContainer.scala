package com.googlecode.yadic

trait ScalaContainer extends Container[ScalaContainer] {
  def add[C](aClass: Class[C], activator: () => C): ScalaContainer
}
