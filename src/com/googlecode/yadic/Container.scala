package com.googlecode.yadic


trait Container {
  def add[C <: Object](concrete:Class[C]): Unit
  def add[I <: Object, C <: I](interface:Class[I], concrete:Class[C]): Unit
  def add[A <: Object](aClass:Class[A], activator:() => A ): Unit
  def decorate[I <: Object, C <: I](interface:Class[I], concrete:Class[C]): Unit
  def resolve( aClass:Class[_] ): Object
  def resolveType[A <: Object]( aClass:Class[A] ): A
}