package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.reflection.Types;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.resolvers.Resolvers.asFunction1;


public class TypeConverter<T> implements Function1<Type, Type> {
    private final Map<TypeVariable, Type> typeMap;

    public TypeConverter(Map<TypeVariable, Type> typeMap) {
        this.typeMap = typeMap;
    }

    public Type call(Type type) throws Exception {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return Types.parameterizedType(parameterizedType.getRawType(), typeArgumentsOf(parameterizedType).map(this));
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            if (!typeMap.containsKey(typeVariable)) {
                throw new UnsupportedOperationException("Unknown TypeVariable " + typeVariable);
            }
            return typeMap.get(typeVariable);
        }
        return type;
    }

    @SuppressWarnings("unchecked")
    public static Object[] convertParametersToInstances(Resolver<?> resolver, Type requiredType, Class concrete, Sequence<Type> genericParameterTypes) {
        return genericParameterTypes.
                map(new TypeConverter(typeMap(requiredType, concrete))).
                map(asFunction1(resolver)).toArray(Object.class);
    }

    public static Map<TypeVariable, Type> typeMap(Type requiredType, Class concreteType) {
        return typeArgumentsOf(concreteType).
                zip(typeArgumentsOf(requiredType)).
                fold(new HashMap<>(), Maps.<TypeVariable, Type>asMap());
    }

    public static Sequence<Type> typeArgumentsOf(Type type) {
        if (type instanceof ParameterizedType) {
            return typeArgumentsOf((ParameterizedType) type);
        }
        return sequence(type);
    }

    public static Sequence<Type> typeArgumentsOf(ParameterizedType type) {
        return sequence(type.getActualTypeArguments());
    }

    public static Sequence<TypeVariable> typeArgumentsOf(Class aClass) {
        return sequence((aClass.getTypeParameters()));
    }

}
