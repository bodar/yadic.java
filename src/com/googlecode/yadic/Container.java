package com.googlecode.yadic;

public interface Container {
  void add(Class concrete);
  void add(Class aInterface, Class concrete);
  void add(Class aClass, Activator activator );
  void decorate(Class i, Class concrete);
  Object resolve(Class aClass);
  <T> T resolveType(Class<T> aClass);
}
