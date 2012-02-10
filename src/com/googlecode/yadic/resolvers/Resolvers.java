package com.googlecode.yadic.resolvers;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.generics.Types.classOf;

public class Resolvers {

    public static <I, C extends I> Resolver<Object> decorator(final TypeMap typeMap, final Class<I> anInterface, final Class<C> concrete) {
        Resolver<Object> remove = typeMap.remove(anInterface);
        return create(concrete, new DecoratorResolver<Object>(anInterface, remove, typeMap));
    }

    public static Resolver<Object> decorator(final TypeMap typeMap, final Type anInterface, final Type concrete) {
        return create(concrete, new DecoratorResolver<Object>(anInterface, typeMap.remove(anInterface), typeMap));
    }

    public static <T, A extends Resolver<T>> Resolver<T> activator(final Resolver resolver, final Class<A> activator) {
        return activatorResolver(activator, resolver);
    }

    public static Resolver<Object> activator(final Resolver resolver, final Type activator) {
        return activatorResolver(activator, resolver);
    }

    public static Resolver<Object> create(final Type t, Resolver<?> resolver) {
        if (classOf(t).getConstructors().length > 0) {
            return constructor(t, resolver);
        }
        return staticMethod(t, resolver);
    }

    public static Resolver<Object> constructor(final Type concrete, final Resolver<?> resolver) {
        return new Resolver<Object>() {
            public Object resolve(Type type) throws Exception {
                return new ConstructorResolver<Object>(resolver, concrete).resolve(type);
            }
        };
    }

    public static Resolver<Object> staticMethod(final Type concrete, final Resolver<?> resolver) {
        return new Resolver<Object>() {
            public Object resolve(Type type) throws Exception {
                return new StaticMethodResolver<Object>(resolver, concrete).resolve(type);
            }
        };
    }

    public static <T> Callable<T> asCallable(final Resolver<? extends T> resolver, final Type type) {
        return asFunction1(resolver).deferApply(type);
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
                throw new ContainerException(type + " cannot be created", exceptions);
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

    public static <T> Function1<Type, T> asFunction1(final Resolver<? extends T> resolver) {
        return new Function1<Type, T>() {
            public T call(Type type) throws Exception {
                return resolver.resolve(type);
            }
        };
    }

    public static <T> Resolver<T> asResolver(final Callable<? extends T> activator) {
        if(activator instanceof Closeable){
            return new ClosableResolver<T>(activator);
        }
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
            throw new ContainerException(type + " cannot be created", e);
        }
    }

    public static Closeable ignore() {
        return new Closeable() {
            public void close() throws IOException {
            }
        };
    }

    public static <T> ActivatorResolver<T> activatorResolver(Type activatorType, Resolver resolver) {
        if( Closeable.class.isAssignableFrom(classOf(activatorType))){
            return new ClosableActivatorResolver<T>(activatorType, resolver);
        }
        return new ActivatorResolver<T>(activatorType, resolver);
    }

    private static class ClosableResolver<T> implements Resolver<T>, Closeable {
        private final Callable<? extends T> activator;

        public ClosableResolver(Callable<? extends T> activator) {
            this.activator = activator;
        }

        public T resolve(Type type) throws Exception {
            return activator.call();
        }

        public void close() throws IOException {
            Closeables.close(activator);
        }
    }
}
