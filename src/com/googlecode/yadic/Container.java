package com.googlecode.yadic;

public interface Container {
  <C> void add(Class<C> concrete);
  <I, C extends I> void add(Class<I> aInterface, Class<C> concrete);
  <T> void add(Class<T> aClass, Activator<T> activator );
  <I, C extends I> void decorate(Class<I> i, Class<C> concrete);
  Object resolve(Class aClass);
  <T> T resolveType(Class<T> aClass);
}
