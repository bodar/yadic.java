package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Types {
    public static ParameterizedType parameterizedType(final Type rawType, final Type... typeArguments) {
        return new AParameterizedType(null, rawType, typeArguments);
    }

    public static Sequence<Type> classTypeParameters(Type concrete) {
        if(concrete instanceof Class){
            return sequence(((Class) concrete).getTypeParameters()).safeCast(Type.class);
        }
        if(concrete instanceof ParameterizedType){
            return sequence(((ParameterizedType) concrete).getActualTypeArguments());
        }
        throw new UnsupportedOperationException();
    }

    public static Class classOf(Type concrete) {
        if(concrete instanceof Class){
            return (Class) concrete;
        }
        if(concrete instanceof ParameterizedType){
            return classOf(((ParameterizedType) concrete).getRawType());
        }
        throw new UnsupportedOperationException();
    }

    public static boolean equalTo(Type a, Type b){
        if(a == null && b == null ){
            return true;
        }

        if(a instanceof Class && b instanceof Class){
            return a.equals(b);
        }

        if(a instanceof ParameterizedType && b instanceof ParameterizedType){
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equalTo(pa.getOwnerType(), pb.getOwnerType()) &&
                    equalTo(pa.getRawType(), pb.getRawType()) &&
                    sequence(pa.getActualTypeArguments()).zip(sequence(pb.getActualTypeArguments())).forAll(equalTo());
        }

        return false;
    }

    public static Predicate<? super Pair<Type, Type>> equalTo() {
        return new Predicate<Pair<Type, Type>>() {
            public boolean matches(Pair<Type, Type> pair) {
                return equalTo(pair.first(), pair.second());
            }
        };
    }

    public static Predicate<? super Type> equalTo(final Type type) {
        return new Predicate<Type>() {
            public boolean matches(Type other) {
                return equalTo(type, other);
            }
        };
    }

}
