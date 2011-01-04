package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.yadic.generics.Types.classTypeParameters;

public class TypeConverter<T> implements Callable1<Type, Type> {
    private final Map<Type, Type> typeMap;

    private TypeConverter(Type type, Constructor<T> constructor) {
        typeMap = sequence(constructor.getGenericParameterTypes()).
                zip(classTypeParameters(type)).
                fold(new HashMap<Type, Type>(), Maps.<Type, Type>asMap());
    }

    public static <T> Callable1<Type, Type> typeConverter(Type type, Constructor<T> constructor) {
        return new TypeConverter<T>(type, constructor);
    }

    public Type call(Type type) throws Exception {
        if(typeMap.containsKey(type)){
            return typeMap.get(type);
        }
        return type;
    }
}
