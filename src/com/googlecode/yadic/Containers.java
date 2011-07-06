package com.googlecode.yadic;

public class Containers {
    public static Container selfRegister(Container container) {
        return container.addInstance(Container.class, container).
                addActivator(Resolver.class, container.getActivator(Container.class));
    }
}
