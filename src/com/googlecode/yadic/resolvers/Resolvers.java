package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Exceptions;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.curry;

public class Resolvers {

    public static <I, C extends I> Resolver<Object> decorator(final TypeMap typeMap, final Class<I> anInterface, final Class<C> concrete) {
        Resolver<Object> remove = typeMap.remove(anInterface);
        return create(concrete, new DecoratorResolver<Object>(anInterface, remove, typeMap));
    }

    public static Resolver<Object> decorator(final TypeMap typeMap, final Type anInterface, final Type concrete) {
        return create(concrete, new DecoratorResolver<Object>(anInterface, typeMap.remove(anInterface), typeMap));
    }

    public static <T, A extends Resolver<T>> Resolver<T> activator(final Resolver resolver, final Class<A> activator) {
        return new ActivatorResolver<T>(activator, resolver);
    }

    public static Resolver<Object> activator(final Resolver resolver, final Type activator) {
        return new ActivatorResolver<Object>(activator, resolver);
    }

    public static Resolver<Object> create(final Type concrete, final Resolver<?> resolver) {
        return new Resolver<Object>() {
            public Object resolve(Type type) throws Exception {
                return new ConstructorResolver<Object>(resolver).resolve(concrete);
            }
        };
    }

    public static Resolver<Object> createByStaticMethod(final Type concrete, final Resolver<?> resolver) {
        return new Resolver<Object>() {
            public Object resolve(Type type) throws Exception {
                return new StaticMethodResolver<Object>(resolver).resolve(concrete);
            }
        };
    }

    public static <T> Callable<T> asCallable(final Resolver<? extends T> resolver, final Type type) {
        return curry(asCallable1(resolver), type);
    }

    public static Resolver<Object> listOf(final Resolver<?>... resolvers) {
        return new Resolver<Object>() {
            public Object resolve(Type type) throws Exception {
                List<Exception> exceptions = new ArrayList<Exception>();
                for (Resolver<?> resolver : resolvers) {
                    try {
                        return resolver.resolve(type);
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                }
                throw new ContainerException("Unable to create " + type, exceptions);
            }
        };
    }

    public static <T> Callable1<Type, T> asCallable1(final Resolver<? extends T> resolver) {
        return new Callable1<Type, T>() {
            public T call(Type type) throws Exception {
                return resolver.resolve(type);
            }
        };
    }

    public static <T> Resolver<T> asResolver(final Callable<? extends T> activator) {
        return new Resolver<T>() {
            public T resolve(Type ignored) throws Exception {
                return activator.call();
            }
        };
    }

    public static <T> Resolver<T> asResolver(final Callable1<Type, ? extends T> activator) {
        return new Resolver<T>() {
            public T resolve(Type type) throws Exception {
                return activator.call(type);
            }
        };
    }

    public static <T> T resolve(Resolver<T> resolver, Type type) {
        try {
            return (T) resolver.resolve(type);
        } catch (ContainerException e) {
            throw e;
        } catch (Exception e) {
            throw new ContainerException(type.toString() + " cannot be created", e);
        }

    }
}
