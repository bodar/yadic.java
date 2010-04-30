package com.googlecode.yadic;

public interface Container {
  <C> Container add(Class<C> concrete);
  <I, C extends I> Container add(Class<I> aInterface, Class<C> concrete);
  <T> Container add(Class<T> aClass, Activator<T> activator );
  <I, C extends I> Container decorate(Class<I> i, Class<C> concrete);
  Object resolve(Class aClass);
  <T> T resolveType(Class<T> aClass);
}
