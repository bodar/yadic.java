package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.functions.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Arrays.exists;
import static com.googlecode.totallylazy.functions.Callables.cast;
import static com.googlecode.totallylazy.functions.Callables.descending;
import static com.googlecode.totallylazy.reflection.Methods.genericParameterTypes;
import static com.googlecode.totallylazy.reflection.Methods.genericReturnType;
import static com.googlecode.totallylazy.reflection.Methods.modifier;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.predicates.Predicates.not;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.generics.TypeConverter.convertParametersToInstances;
import static com.googlecode.yadic.generics.Types.classOf;
import static com.googlecode.yadic.generics.Types.matches;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

@SuppressWarnings("unchecked")
public class StaticMethodResolver<T> implements Resolver<T> {
    private final Resolver<?> resolver;
    private final Class<T> concreteClass;

    private StaticMethodResolver(Resolver<?> resolver, Type concrete) {
        this.resolver = resolver;
        this.concreteClass = classOf(concrete);
    }

    public static <T> StaticMethodResolver<T> staticMethodResolver(Resolver<?> resolver, Type concrete) {
        return new StaticMethodResolver<T>(resolver, concrete);
    }

    public T resolve(Type type) throws Exception {
        Sequence<Method> methods = sequence(concreteClass.getMethods()).
                filter(modifier(PUBLIC).and(modifier(STATIC)).
                        and(where(genericReturnType(), matches(type)).
                                and(where(genericParameterTypes(), not(exists(matches(type))))))).
                sortBy(descending(arity()));

        if (methods.isEmpty()) {
            throw new ContainerException(concreteClass.getName() + " does not have any public static methods that return " + type);
        }
        final List<Exception> exceptions = new ArrayList<Exception>();
        return methods.tryPick(firstSatisfiableMethod(exceptions, type)).map(cast(concreteClass)).
                getOrElse(Callables.<T>callThrows(new ContainerException(format("Could not resolve %s using static factory methods- please check root exception for details", concreteClass.getName()), exceptions)));
    }

    private Function1<Method, Comparable> arity() {
        return method -> method.getParameterTypes().length;
    }

    private Function1<Method, Option<Object>> firstSatisfiableMethod(final List<Exception> exceptions, final Type type) {
        return method -> {
            try {
                Object[] instances = convertParametersToInstances(resolver, type, concreteClass, sequence(method.getGenericParameterTypes()));
                return some(method.invoke(null, instances));
            } catch (Exception e) {
                exceptions.add(e);
                return none();
            }
        };
    }
}