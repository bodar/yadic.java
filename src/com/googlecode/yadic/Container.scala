package com.googlecode.yadic;

trait Container extends Resolver {
  def add[C](concrete: Class[C]): Container
  def add[I, C <: I](interface: Class[I], concrete: Class[C]): Container
  def add[T](a: Class[T], activator: Activator[T]): Container
  def add[T](aClass: Class[T], activator: () => T): Container
  def resolveType[A](aClass: Class[A]): A
}
