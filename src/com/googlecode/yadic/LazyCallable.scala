package com.googlecode.yadic

import java.util.concurrent.Callable

class LazyActivator[T](callable: () => T) extends Callable[T] {
  lazy val call = callable()
}