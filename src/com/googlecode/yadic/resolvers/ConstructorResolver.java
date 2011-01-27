package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.*;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Arrays.exists;
import static com.googlecode.totallylazy.Callables.cast;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Constructors.genericParameterTypes;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.generics.TypeConverter.convertParametersToInstances;
import static com.googlecode.yadic.generics.Types.matches;
import static com.googlecode.yadic.resolvers.Resolvers.asCallable1;
import static com.googlecode.yadic.generics.TypeConverter.typeConverter;
import static com.googlecode.yadic.generics.Types.classOf;

public class ConstructorResolver<T> implements Resolver<T> {
    private final Resolver<?> resolver;

    public ConstructorResolver(Resolver<?> resolver) {
        this.resolver = resolver;
    }

    public T resolve(Type type) throws Exception {
        Class<T> concrete = classOf(type);
        Sequence<Constructor<?>> constructors = sequence(concrete.getConstructors()).
                filter(where(genericParameterTypes(), not(exists(matches(type))))).
                sortBy(descending(numberOfParamters()));
        if (constructors.isEmpty()) {
            throw new ContainerException(concrete.getName() + " does not have a public constructor");
        }
        final List<Exception> exceptions = new ArrayList<Exception>();
        return constructors.tryPick(firstSatisfiableConstructor(exceptions, type)).map(cast(concrete)).
                getOrElse(Callables.<T>callThrows(new ContainerException(concrete.getName() + " does not have a satisfiable constructor", exceptions)));
    }

    private Callable1<Constructor<?>, Option<Object>> firstSatisfiableConstructor(final List<Exception> exceptions, final Type type) {
        return new Callable1<Constructor<?>, Option<Object>>() {
            public Option<Object> call(Constructor<?> constructor) throws Exception {
                try {
                    Object[] instances = convertParametersToInstances(resolver, type, sequence(constructor.getGenericParameterTypes()));
                    return some(constructor.newInstance(instances));
                } catch (Exception e) {
                    exceptions.add(e);
                    return none();
                }
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
