package com.googlecode.yadic

import java.util.concurrent.Callable

class LazyCallable[T](callable: () => T) extends Callable[T] {
  lazy val call = callable()
}