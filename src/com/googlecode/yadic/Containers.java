package com.googlecode.yadic;

import com.googlecode.yadic.generics.Types;
import com.googlecode.yadic.resolvers.DecoratorResolver;

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
        return (Container) container.addType(anInterface, activator(asCreator(decoratorResolver, activator), activator));
    }

    private static Creator asCreator(final Resolver<?> resolver, final Class<?> activator) {
        return new Creator() {
            public <T> T create(Type type) throws Exception {
                if(Types.matches(type, activator)){
                    return new SimpleContainer(resolver).create(activator);
                }
                return (T) resolver.resolve(type);
            }
        };
    }
}
