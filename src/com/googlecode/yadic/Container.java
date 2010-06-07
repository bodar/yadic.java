package com.googlecode.yadic;

public interface Container<T extends Container<T>> extends Resolver{
  <C> T add(Class<C> concrete); 

  <I, C extends I> T add(Class<I> anInterface, Class<C> concrete);

  <C> T add(Class<C> aClass, Activator<C> activator);

  T addInstance(Object instance);

  <C, A extends Activator<C>> T addActivator(Class<C> aClass, Class<A> activator);

  <I, C extends I> T decorate(Class<I> anInterface, Class<C> concrete);

  <C> Activator<C> remove(Class<C> aClass);

  <C> boolean contains(Class<C> aClass);

  <A> A resolveType(Class<A> aClass);
}
