package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.generics.TypeConverter;

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
import static com.googlecode.yadic.generics.Types.classOf;
import static com.googlecode.yadic.generics.Types.matches;
import static java.lang.String.format;

public class ConstructorResolver<T> implements Resolver<T> {
    private final Resolver<?> resolver;
    private final Type concrete;
    private final Class<T> concreteClass;

    public ConstructorResolver(Resolver<?> resolver, Type concrete) {
        this.resolver = resolver;
        this.concrete = concrete;
        concreteClass = classOf(concrete);
    }

    public T resolve(Type type) throws Exception {
        Sequence<Constructor<?>> constructors = sequence(concreteClass.getConstructors()).
                filter(where(genericParameterTypes(), not(exists(matches(concrete))))).
                filter(constructorsWithUniqueParamTypes()).
                sortBy(descending(numberOfParameters()));
        if (constructors.isEmpty()) {
            throw new ContainerException(concreteClass.getName() + " does not have a public constructor");
        }
        final List<Exception> exceptions = new ArrayList<Exception>();
        return constructors.tryPick(firstSatisfiableConstructor(exceptions, type)).map(cast(concreteClass)).
                getOrElse(Callables.<T>callThrows(new ContainerException(format("Could not resolve %s- please check root exception for details.", concreteClass.getName()), exceptions)));
    }

    private Predicate<? super Constructor<?>> constructorsWithUniqueParamTypes() {
        return new Predicate<Constructor<?>>() {
            public boolean matches(Constructor<?> constructor) {
                final Sequence<Type> types = sequence(constructor.getGenericExceptionTypes());
                return types.unique().equals(types);
            }
        };
    }

    private Callable1<Constructor<?>, Option<Object>> firstSatisfiableConstructor(final List<Exception> exceptions, final Type type) {
        return new Callable1<Constructor<?>, Option<Object>>() {
            public Option<Object> call(Constructor<?> constructor) throws Exception {
                try {
                    Object[] instances = TypeConverter.convertParametersToInstances(resolver, type, concreteClass, sequence(constructor.getGenericParameterTypes()));
                    return some(constructor.newInstance(instances));
                } catch (Exception e) {
                    exceptions.add(e);
                    return none();
                }
            }
        };
    }

    private Callable1<Constructor<?>, Comparable> numberOfParameters() {
        return new Callable1<Constructor<?>, Comparable>() {
            public Comparable call(Constructor<?> constructor) throws Exception {
                return constructor.getParameterTypes().length;
            }
        };
    }
}
