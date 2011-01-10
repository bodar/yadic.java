package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.generics.Types.classTypeParameters;
import static com.googlecode.yadic.resolvers.Resolvers.asCallable1;

public class TypeConverter<T> implements Callable1<Type, Type> {
    private final Map<Type, Type> typeMap;

    private TypeConverter(Type type, final Sequence<Type> genericParameterTypes) {
        typeMap = genericParameterTypes.
                zip(classTypeParameters(type)).
                fold(new HashMap<Type, Type>(), Maps.<Type, Type>asMap());
    }

    public static <T> Callable1<Type, Type> typeConverter(Type type, final Sequence<Type> genericParameterTypes) {
        return new TypeConverter<T>(type, genericParameterTypes);
    }

    public static Object[] convertParametersToInstances(final Resolver<?> resolver, Type type, final Sequence<Type> genericParameterTypes) {
        return genericParameterTypes.
                map(typeConverter(type, genericParameterTypes)).
                map(asCallable1(resolver)).toArray(Object.class);
    }

    public Type call(Type type) throws Exception {
        if(typeMap.containsKey(type)){
            return typeMap.get(type);
        }
        return type;
    }
}
