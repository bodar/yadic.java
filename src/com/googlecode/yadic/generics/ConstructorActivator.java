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
import static com.googlecode.totallylazy.Callables.returnArgument;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;

public class ConstructorActivator<T> implements Callable<T> {
    private final Class<T> concrete;
    private final TypeToCallableFactory factory;
    private final Callable1<Type, Type> typeConverter;

    private ConstructorActivator(Type type, Class<T> concrete, TypeToCallableFactory factory) {
        this.concrete = concrete;
        this.factory = factory;
        typeConverter = type instanceof ParameterizedType ? new TypeConverter<T>((ParameterizedType) type, concrete) : returnArgument(Type.class);
    }

    public static <T> ConstructorActivator<T> create(final Type type, Class<T> concrete, final Resolver resolver) {
        return create(type, concrete, new TypeToCallableFactory(resolver));
    }

    public static <T> ConstructorActivator<T> create(final Type type, Class<T> concrete, final TypeToCallableFactory factory) {
        return new ConstructorActivator<T>(type, concrete, factory);
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
        return sequence(constructor.getGenericParameterTypes()).map(typeConverter);
    }

    private Callable1<Constructor<?>, Comparable> numberOfParamters() {
        return new Callable1<Constructor<?>, Comparable>() {
            public Comparable call(Constructor<?> constructor) throws Exception {
                return constructor.getParameterTypes().length;
            }
        };
    }

    private static class TypeConverter<T> implements Callable1<Type, Type> {
        private final Map<TypeVariable<Class<T>>, Type> typeVariableMap;

        public TypeConverter(ParameterizedType parameterizedType, Class<T> concrete) {
            typeVariableMap = sequence(concrete.getTypeParameters()).
                    zip(sequence(parameterizedType.getActualTypeArguments())).
                    fold(new HashMap<TypeVariable<Class<T>>, Type>(), Maps.<TypeVariable<Class<T>>, Type>asMap());
        }

        public Type call(Type type) throws Exception {
            if(typeVariableMap.containsKey(type)){
                return typeVariableMap.get(type);
            }
            return type;
        }
    }
}
