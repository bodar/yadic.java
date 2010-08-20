package com.googlecode.yadic;

import scala.Function0;

public interface Container extends Resolver {
    <Concrete> Container add(Class<Concrete> concrete);

    <Interface, Concrete extends Interface> Container add(Class<Interface> anInterface, Class<Concrete> concrete);

    Container addInstance(Object instance);

    <Interface, Concrete extends Interface> Container addInstance(Class<Interface> anInterface, Concrete instance);

    <Type> Container addActivator(Class<Type> aClass, Activator<Type> activator);

    <Type, AnActivator extends Activator<Type>> Container addActivator(Class<Type> aClass, Class<AnActivator> activator);

    <Type> Container add(Class<Type> aClass, Function0<Type> activator);

    <Interface, Concrete extends Interface> Container decorate(Class<Interface> anInterface, Class<Concrete> concrete);

    <Type> Activator<Type> remove(Class<Type> aClass);

    <Type> boolean contains(Class<Type> aClass);

    <Type> Type resolveType(Class<Type> aClass);

    <Type> Activator<Type> getActivator(Class<Type> aClass);
}
