package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Maps;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Callables.returnArgument;
import static com.googlecode.totallylazy.Sequences.sequence;

public class TypeConverter<T> implements Callable1<Type, Type> {
    private final Map<TypeVariable<Class<T>>, Type> typeVariableMap;

    private TypeConverter(ParameterizedType parameterizedType, Class<T> concrete) {
        typeVariableMap = sequence(concrete.getTypeParameters()).
                zip(sequence(parameterizedType.getActualTypeArguments())).
                fold(new HashMap<TypeVariable<Class<T>>, Type>(), Maps.<TypeVariable<Class<T>>, Type>asMap());
    }

    public static <T> Callable1<Type, Type> typeConverter(Type type, Class<T> concrete) {
        return type instanceof ParameterizedType ? new TypeConverter<T>((ParameterizedType) type, concrete) : returnArgument(Type.class);
    }


    public Type call(Type type) throws Exception {
        if(typeVariableMap.containsKey(type)){
            return typeVariableMap.get(type);
        }
        return type;
    }
}
