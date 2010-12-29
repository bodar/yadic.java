package com.googlecode.yadic;

import com.googlecode.totallylazy.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.cast;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CreateCallable<T> implements Callable<T> {
    private final Class<T> concrete;
    private final TypeToCallableFactory factory;

    private CreateCallable(Class<T> concrete, TypeToCallableFactory factory) {
        this.concrete = concrete;
        this.factory = factory;
    }

    public static <T> CreateCallable<T> create(final Class<T> concrete, final Resolver resolver) {
        return create(concrete, new TypeToCallableFactory(resolver));
    }

    public static <T> CreateCallable<T> create(final Class<T> concrete, final TypeToCallableFactory factory) {
        return new CreateCallable<T>(concrete, factory);
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
                    Sequence<Object> instances = sequence(constructor.getGenericParameterTypes()).map(factory.convertToCallable());
                    return some(constructor.newInstance(instances.toArray(Object.class)));
                } catch (ContainerException e) {
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
