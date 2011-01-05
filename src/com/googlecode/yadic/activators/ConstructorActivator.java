package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Callables.cast;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.generics.TypeConverter.typeConverter;
import static com.googlecode.yadic.generics.Types.classOf;

public class ConstructorActivator<T> implements Callable1<Type, T> {
    private final Type type;
    private final Class<T> concrete;
    private final Resolver resolver;

    public ConstructorActivator(Resolver resolver, Type type) {
        this.type = type;
        this.concrete = classOf(type);
        this.resolver = resolver;
    }

    public T call(Type type) throws Exception {
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
                    Sequence<Object> instances = genericParametersFor(constructor).map(toInstance());
                    return some(constructor.newInstance(instances.toArray(Object.class)));
                } catch (ContainerException e) {
                    exceptions.add(e);
                    return none();
                }
            }
        };
    }

    private Sequence<Type> genericParametersFor(Constructor<?> constructor) {
        return sequence(constructor.getGenericParameterTypes()).map(typeConverter(type, constructor));
    }

    private Callable1<Constructor<?>, Comparable> numberOfParamters() {
        return new Callable1<Constructor<?>, Comparable>() {
            public Comparable call(Constructor<?> constructor) throws Exception {
                return constructor.getParameterTypes().length;
            }
        };
    }

    private Callable1<? super Type, Object> toInstance() {
        return new Callable1<Type, Object>() {
            public Object call(Type type) throws Exception {
                return resolver.resolve(type);
            }
        };
    }
}
