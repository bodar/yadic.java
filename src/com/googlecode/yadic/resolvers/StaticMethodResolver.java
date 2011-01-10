package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.*;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.generics.Types;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Callables.cast;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.modifier;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.generics.TypeConverter.convertParametersToInstances;
import static com.googlecode.yadic.generics.TypeConverter.typeConverter;
import static com.googlecode.yadic.generics.Types.classOf;
import static com.googlecode.yadic.resolvers.Resolvers.asCallable1;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

public class StaticMethodResolver<T> implements Resolver<T> {
    private final Resolver<?> resolver;

    public StaticMethodResolver(Resolver<?> resolver) {
        this.resolver = resolver;
    }

    public T resolve(Type type) throws Exception {
        Class<T> concrete = classOf(type);
        Sequence<Method> methods = sequence(concrete.getMethods()).filter(modifier(PUBLIC).and(modifier(STATIC)).and(returnType(type)));
        if (methods.isEmpty()) {
            throw new ContainerException(concrete.getName() + " does not have any public static methods that return " + type);
        }
        final List<ContainerException> exceptions = new ArrayList<ContainerException>();
        return methods.tryPick(firstSatisfiableMethod(exceptions, type)).map(cast(concrete)).
                getOrElse(Callables.<T>callThrows(new ContainerException(concrete.getName() + " does not have a satisfiable public static method", exceptions)));
    }

    private Predicate<? super Method> returnType(final Type type) {
        return new Predicate<Method>() {
            public boolean matches(Method method) {
                return Types.matches(type, method.getGenericReturnType());
            }
        };
    }

    private Callable1<Method, Option<Object>> firstSatisfiableMethod(final List<ContainerException> exceptions, final Type type) {
        return new Callable1<Method, Option<Object>>() {
            public Option<Object> call(Method method) throws Exception {
                try {
                    Object[] instances = convertParametersToInstances(resolver, type, sequence(method.getGenericParameterTypes()));
                    return some(method.invoke(null, instances));
                } catch (ContainerException e) {
                    exceptions.add(e);
                    return none();
                }
            }
        };
    }
}
