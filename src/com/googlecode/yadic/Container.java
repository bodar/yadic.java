package com.googlecode.yadic;

import scala.Function0;

import java.util.concurrent.Callable;

public interface Container extends Resolver {
    <Concrete> Container add(Class<Concrete> concrete);

    <Interface, Concrete extends Interface> Container add(Class<Interface> anInterface, Class<Concrete> concrete);

    Container addInstance(Object instance);

    <Interface, Concrete extends Interface> Container addInstance(Class<Interface> anInterface, Concrete instance);

    <Type, ExtendsType extends Type> Container addCallable(Class<Type> aClass, Callable<ExtendsType> callable);

    <Type, ACallable extends Callable<Type>> Container addCallable(Class<Type> aClass, Class<ACallable> callable);

    <Type> Container add(Class<Type> aClass, Function0<Type> function);

    <Interface, Concrete extends Interface> Container decorate(Class<Interface> anInterface, Class<Concrete> concrete);

    <Type> Callable<Type> remove(Class<Type> aClass);

    <Type> boolean contains(Class<Type> aClass);

    <Type> Type get(Class<Type> aClass);

    <Type> Callable<Type> getCallable(Class<Type> aClass);
}
