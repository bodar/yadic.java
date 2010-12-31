package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.*;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeToCallableFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.cast;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CreateParameterizedType<T> implements Callable<T> {
    private final Class<T> concrete;
    private final TypeToCallableFactory factory;
    private final Map<TypeVariable<Class<T>>, Type> typeVariableMap;

    private CreateParameterizedType(Class<T> concrete, ParameterizedType parameterizedType, TypeToCallableFactory factory) {
        this.concrete = concrete;
        this.factory = factory;
        typeVariableMap = sequence(concrete.getTypeParameters()).
                zip(sequence(parameterizedType.getActualTypeArguments())).
                fold(new HashMap<TypeVariable<Class<T>>, Type>(), Maps.<TypeVariable<Class<T>>, Type>asMap());

    }

    public static <T> CreateParameterizedType<T> createParameterizedType(Class<T> concrete, final ParameterizedType parameterizedType, final Resolver resolver) {
        return createParameterizedType(concrete, parameterizedType, new TypeToCallableFactory(resolver));
    }

    public static <T> CreateParameterizedType<T> createParameterizedType(Class<T> concrete, final ParameterizedType parameterizedType, final TypeToCallableFactory factory) {
        return new CreateParameterizedType<T>(concrete, parameterizedType, factory);
    }

    public T call() throws Exception {
        Sequence<Constructor<?>> constructors = sequence(concrete.getConstructors()).sortBy(descending(numberOfParamters()));
        if (constructors.isEmpty()) {
            throw new ContainerException(concrete.getName() + " does not have a public constructor");
        }
        final List<ContainerException> exceptions = new ArrayList<ContainerException>();
        return constructors.tryPick(firstSatisfiableConstructor(exceptions)).map(cast(concrete)).
                getOrElse(Callables.<T>callThrows(new ContainerException(concrete.getName() + " does not have a satisfiable constructor", exceptions)));
    }

    private Callable1<Constructor<?>, Option<Object>> firstSatisfiableConstructor(final List<ContainerException> exceptions) {
        return new Callable1<Constructor<?>, Option<Object>>() {
            public Option<Object> call(Constructor<?> constructor) throws Exception {
                try {
                    Sequence<Object> instances = genericParametersFor(constructor).map(factory.convertToCallable());
                    return some(constructor.newInstance(instances.toArray(Object.class)));
                } catch (ContainerException e) {
                    exceptions.add(e);
                    return none();
                }
            }
        };
    }

    private Sequence<Type> genericParametersFor(Constructor<?> constructor) {
        return sequence(constructor.getGenericParameterTypes()).map(replaceTypeVariables());
    }

    private Callable1<? super Type, Type> replaceTypeVariables() {
        return new Callable1<Type, Type>() {
            public Type call(Type type) throws Exception {
                if(typeVariableMap.containsKey(type)){
                    return typeVariableMap.get(type);
                }
                return type;
            }
        };
    }

    private Callable1<? super TypeVariable<Class<T>>, Object> getName() {
        return new Callable1<TypeVariable<Class<T>>, Object>() {
            public Object call(TypeVariable<Class<T>> classTypeVariable) throws Exception {
                return classTypeVariable.getName();
            }
        };
    }

    private Callable1<Constructor<?>, Comparable> numberOfParamters() {
        return new Callable1<Constructor<?>, Comparable>() {
            public Comparable call(Constructor<?> constructor) throws Exception {
                return constructor.getParameterTypes().length;
            }
        };
    }

}
