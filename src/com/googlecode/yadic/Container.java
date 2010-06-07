package com.googlecode.yadic;

import scala.Function0;

public interface Container extends Resolver{
  <C> Container add(Class<C> concrete); 

  <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete);

  <C> Container add(Class<C> aClass, Activator<C> activator);

  Container addInstance(Object instance);

  <C, A extends Activator<C>> Container addActivator(Class<C> aClass, Class<A> activator);
    
  <C> Container add(Class<C> aClass, Function0<C> activator);

  <I, C extends I> Container decorate(Class<I> anInterface, Class<C> concrete);

  <C> Activator<C> remove(Class<C> aClass);

  <C> boolean contains(Class<C> aClass);

  <A> A resolveType(Class<A> aClass);
}
