package com.googlecode.yadic;

import com.googlecode.totallylazy.Callers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.totallylazy.callables.LazyCallable.lazy;
import static com.googlecode.yadic.CreateCallable.create;
import static java.util.Arrays.asList;

public class SimpleContainer implements Container {
    private final Map<Class, Callable> activators = new HashMap<Class, Callable>();
    private final Resolver missingHandler;

    public SimpleContainer(Resolver missingHandler) {
        this.missingHandler = missingHandler;
    }

    public SimpleContainer() {
        this(new Resolver() {
            public Object resolve(Class aClass) {
                throw new ContainerException(aClass.getName() + " not found in container");
            }
        });
    }

    public Object resolve(Class aClass) {
        if (!activators.containsKey(aClass)) {
            return missingHandler.resolve(aClass);
        }
        try {
            return activators.get(aClass).call();
        } catch (ContainerException e) {
            throw e;
        } catch (Exception e) {
            throw new ContainerException(aClass.getName() + " cannot be created", e);
        }
    }

    public <T> T get(Class<T> aClass) {
        return (T) resolve(aClass);
    }

    public <T> Callable<T> getActivator(Class<T> aClass) {
        return activators.get(aClass);
    }

    public <T> Container add(final Class<T> concrete) {
        return addActivator(concrete, create(concrete, this));
    }

    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        return addActivator(anInterface, create(concrete, this));
    }

    public <I, C extends I> Container addInstance(Class<I> anInterface, C instance) {
        return addActivator(anInterface, returns(instance));
    }

    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, final Class<A> activator) {
        return add(activator).addActivator(aClass, new Callable<T>() {
            public T call() throws Exception {
                return get(activator).call();
            }
        });
    }

    public <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator) {
        if (activators.containsKey(aClass)) {
            throw new ContainerException(aClass.getName() + " already added to container");
        }
        activators.put(aClass, lazy(activator));
        return this;
    }

    public <I, C extends I> Container decorate(final Class<I> anInterface, final Class<C> concrete) {
        final Callable<?> existing = activators.get(anInterface);
        activators.put(anInterface, lazy(create(concrete, new Resolver() {
            public Object resolve(Class aClass) {
                return aClass.equals(anInterface) ? Callers.call(existing) : SimpleContainer.this.resolve(aClass);
            }
        })));
        return this;
    }

    public <T> Callable<T> remove(Class<T> aClass) {
        return activators.remove(aClass);
    }

    public <T> boolean contains(Class<T> aClass) {
        return activators.containsKey(aClass);
    }
}