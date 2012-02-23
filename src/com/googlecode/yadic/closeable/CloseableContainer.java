package com.googlecode.yadic.closeable;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.DelegatingContainer;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.resolvers.MissingResolver;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.closeable.CloseableTypeMap.isCloseable;

public abstract class CloseableContainer extends DelegatingContainer implements CloseableMap<CloseableContainer> {
    private final CloseableTypeMap typeMap;

    protected CloseableContainer(CloseableTypeMap typeMap) {
        super(SimpleContainer.container(typeMap));
        this.typeMap = typeMap;
    }

    public static CloseableContainer closeableContainer() {
        return closeableContainer(new MissingResolver());
    }

    public static CloseableContainer closeableContainer(Resolver<?> parent) {
        final CloseableTypeMap typeMap = new CloseableTypeMap(parent);
        return new CloseableContainer(typeMap) {
            public void close() throws IOException {
                typeMap.close();
            }
        };
    }

    @Override
    protected CloseableContainer self() {
        return this;
    }


    @Override
    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, Class<A> activator) {
        super.addActivator(aClass, activator);
        if (isCloseable(activator)) {
            removeCloseable(aClass);
        }
        return self();
    }

    @Override
    public <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator) {
        super.addActivator(aClass, activator);
        if (activator instanceof Closeable && !activator.getClass().equals(aClass)) {
            addCloseable(aClass, (Closeable) activator);
        }
        return self();
    }

    public CloseableContainer addCloseable(Type type, Closeable closeable) {
        typeMap.addCloseable(type, closeable);
        return self();
    }

    public <T> CloseableContainer removeCloseable(Type type) {
        typeMap.removeCloseable(type);
        return self();
    }

}
