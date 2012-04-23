package com.googlecode.yadic;

import com.googlecode.yadic.closeable.CloseableContainer;
import com.googlecode.yadic.generics.Types;
import com.googlecode.yadic.resolvers.DecoratorResolver;
import com.googlecode.yadic.resolvers.MissingResolver;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.resolvers.Resolvers.activator;

public class Containers {
    public static Container selfRegister(Container container) {
        return container.addInstance(Container.class, container).
                addActivator(Resolver.class, container.getActivator(Container.class));
    }

    public static <I> Container decorateUsingActivator(final Container container, final Class<I> anInterface, final Class<? extends Callable<? extends I>> activator) {
        final Resolver<Object> existing = container.remove(anInterface);
        final DecoratorResolver decoratorResolver = new DecoratorResolver(anInterface, existing, container);
        return (Container) container.addType(anInterface, activator(asResolver(decoratorResolver, activator), activator));
    }

    private static <I> TypeMap asResolver(final DecoratorResolver decoratorResolver, final Class<? extends Callable<? extends I>> activator) {
        return new BaseTypeMap(new Resolver<Object>() {
            public Object resolve(Type type) throws Exception {
                if(Types.matches(type, activator)){
                    return new SimpleContainer(decoratorResolver).create(activator);
                }
                return decoratorResolver.resolve(type);

            }
        });
    }

    public static Container container() {
        return new SimpleContainer();
    }

    public static Container container(Resolver<?> parent) {
        return new SimpleContainer(parent);
    }

    public static CloseableContainer closeableContainer() {
        return CloseableContainer.closeableContainer();
    }

    public static CloseableContainer closeableContainer(Resolver<?> parent) {
        return CloseableContainer.closeableContainer(parent);
    }

    public static Container addIfAbsent(Container container, Class<?> aClass) {
        if (!container.contains(aClass)) return container.add(aClass);
        return container;
    }

    public static <I, C extends I> Container addIfAbsent(Container container, Class<I> anInterface, Class<C> concrete) {
        if (!container.contains(anInterface)) return container.add(anInterface, concrete);
        return container;
    }

    public static <I, C extends I> Container addInstanceIfAbsent(Container container, Class<I> anInterface, C instance) {
        if (!container.contains(anInterface)) return container.addInstance(anInterface, instance);
        return container;
    }

    public static <T, A extends Callable<T>> Container addActivatorIfAbsent(Container container, Class<T> aClass, Class<A> activator) {
        if (!container.contains(aClass)) return container.addActivator(aClass, activator);
        return container;
    }



}
