package com.googlecode.yadic;

import com.googlecode.yadic.resolvers.DecoratorResolver;

import java.util.concurrent.Callable;

import static com.googlecode.yadic.resolvers.Resolvers.activator;

public class Containers {
    public static Container selfRegister(Container container) {
        return container.addInstance(Container.class, container).
                addActivator(Resolver.class, container.getActivator(Container.class));
    }

    public static <I> Container decorateUsingActivator(Container container, final Class<I> anInterface, final Class<? extends Callable<? extends I>> activator){
        Resolver<Object> existing = container.remove(anInterface);
        return (Container) container.add(anInterface, activator(new DecoratorResolver(anInterface, existing, container), activator));
    }
}
