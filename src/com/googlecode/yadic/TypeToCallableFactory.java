package com.googlecode.yadic;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.TypeToCallableFactory.Rule.rule;
import static java.lang.String.format;

public class TypeToCallableFactory {
    private final Resolver resolver;
    private final List<Rule> rules = new ArrayList<Rule>();

    public TypeToCallableFactory(final Resolver resolver) {
        this.resolver = resolver;
        rules.add(rule(instanceOf(Class.class), classCallable(resolver)));
        rules.add(rule(instanceOfParameterizedTypeWithRawType(Option.class), parameterizedTypeCallable(resolver)));
    }

    private Predicate<? super Type> instanceOfParameterizedTypeWithRawType(final Class aClass) {
        return new Predicate<Type>() {
            public boolean matches(Type type) {
                return type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(aClass);
            }
        };
    }

    private Callable1<Type, Object> parameterizedTypeCallable(final Resolver resolver) {
        return new Callable1<Type, Object>() {
            public Object call(Type type) throws Exception {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> aClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                return new OptionActivator(aClass, resolver).call();
            }
        };
    }

    private Callable1<Type, Object> classCallable(final Resolver resolver) {
        return new Callable1<Type, Object>() {
            public Object call(Type type) throws Exception {
                return resolver.resolve((Class) type);
            }
        };
    }

    public Callable1<? super Type, Object> convertToCallable() {
        return new Callable1<Type, Object>() {
            public Object call(Type type) throws Exception {
                return rules().find(matches(type)).get().call(type);
            }
        };
    }

    private Sequence<Rule> rules() {
        return sequence(rules);
    }

    public static class Rule implements Predicate<Type>, Callable1<Type, Object> {
        private final Predicate<? super Type> predicate;
        private final Callable1<Type, Object> callable;

        private Rule(Predicate<? super Type> predicate, Callable1<Type, Object> callable) {
            this.predicate = predicate;
            this.callable = callable;
        }

        public static Rule rule(Predicate<? super Type> predicate, Callable1<Type, Object> callable) {
            return new Rule(predicate, callable);
        }

        public boolean matches(Type type) {
            return predicate.matches(type);
        }

        public Object call(Type type) throws Exception {
            return callable.call(type);
        }
    }
}
