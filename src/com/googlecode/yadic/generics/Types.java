package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.numbers.Numbers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Types {
    public static ParameterizedType parameterizedType(final Type rawType, final Type... typeArguments) {
        return new AParameterizedType(null, rawType, typeArguments);
    }

    public static Sequence<Type> classTypeParameters(Type concrete) {
        if (concrete instanceof Class) {
            return sequence(((Class) concrete).getTypeParameters()).safeCast(Type.class);
        }
        if (concrete instanceof ParameterizedType) {
            return sequence(((ParameterizedType) concrete).getActualTypeArguments());
        }
        throw new UnsupportedOperationException();
    }

    public static Class classOf(Type concrete) {
        if (concrete instanceof Class) {
            return (Class) concrete;
        }
        if (concrete instanceof ParameterizedType) {
            return classOf(((ParameterizedType) concrete).getRawType());
        }
        throw new UnsupportedOperationException();
    }

    public static List<Type> typeArgumentsOf(Type type) {
        List<Type> types = new ArrayList<Type>();
        if(type instanceof ParameterizedType){
            for (Type subType : ((ParameterizedType) type).getActualTypeArguments()) {
                types.addAll(typeArgumentsOf(subType));
            }
            return types;
        }

        if(type instanceof Class) {
            types.add(type);
            return types;
        }

        throw new UnsupportedOperationException("Does not support " + type.toString());
    }

    public static boolean equalTo(Type a, Type b) {
        if (a == null && b == null) {
            return true;
        }

        if (a instanceof Class && b instanceof Class) {
            return a.equals(b);
        }

        if (a instanceof ParameterizedType && b instanceof ParameterizedType) {
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equalTo(pa.getOwnerType(), pb.getOwnerType()) &&
                    equalTo(pa.getRawType(), pb.getRawType()) &&
                    sequence(pa.getActualTypeArguments()).zip(sequence(pb.getActualTypeArguments())).forAll(equalTo());
        }

        if (a instanceof WildcardType && b instanceof WildcardType) {
            WildcardType aWildcard = (WildcardType) a;
            WildcardType bWildcard = (WildcardType) b;
            return sequence(aWildcard.getUpperBounds()).zip(sequence(bWildcard.getUpperBounds())).forAll(equalTo()) &&
                    sequence(aWildcard.getLowerBounds()).zip(sequence(bWildcard.getLowerBounds())).forAll(equalTo());
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

    public static boolean matches(Type concrete, Type possibleWildCard) {
        if (concrete == null && possibleWildCard == null) {
            return true;
        }

        if (concrete instanceof Class && possibleWildCard instanceof Class) {
            return concrete.equals(possibleWildCard);
        }

        if (concrete instanceof ParameterizedType && possibleWildCard instanceof ParameterizedType) {
            ParameterizedType pa = (ParameterizedType) concrete;
            ParameterizedType pb = (ParameterizedType) possibleWildCard;
            return matches(pa.getOwnerType(), pb.getOwnerType()) &&
                    matches(pa.getRawType(), pb.getRawType()) &&
                    sequence(pa.getActualTypeArguments()).zip(sequence(pb.getActualTypeArguments())).forAll(matches());
        }

        if (concrete instanceof WildcardType && possibleWildCard instanceof WildcardType) {
            WildcardType aWildcard = (WildcardType) concrete;
            WildcardType bWildcard = (WildcardType) possibleWildCard;
            return sequence(aWildcard.getUpperBounds()).zip(sequence(bWildcard.getUpperBounds())).forAll(matches()) &&
                    sequence(aWildcard.getLowerBounds()).zip(sequence(bWildcard.getLowerBounds())).forAll(matches());
        }

        if (possibleWildCard instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) possibleWildCard;
            return withInUpperBounds(concrete, sequence(wildcardType.getUpperBounds())) &&
                    withInLowerBounds(concrete, sequence(wildcardType.getLowerBounds()));
        }


        return false;
    }

    private static boolean withInUpperBounds(Type concrete, Sequence<Type> upperBounds) {
        if (upperBounds.isEmpty()) {
            return true;
        }
        if (Numbers.equalTo(upperBounds.size(), 1)) {
            return (classOf(upperBounds.first())).isAssignableFrom(classOf(concrete));
        }
        throw new UnsupportedOperationException();
    }

    private static boolean withInLowerBounds(Type concrete, Sequence<Type> lowerBounds) {
        if (lowerBounds.isEmpty()) {
            return true;
        }
        if (Numbers.equalTo(lowerBounds.size(), 1)) {
            return (classOf(concrete)).isAssignableFrom(classOf(lowerBounds.first()));
        }
        throw new UnsupportedOperationException();
    }

    public static Predicate<? super Pair<Type, Type>> matches() {
        return new Predicate<Pair<Type, Type>>() {
            public boolean matches(Pair<Type, Type> pair) {
                return Types.matches(pair.first(), pair.second());
            }
        };
    }

    public static Predicate<? super Type> matches(final Type type) {
        return new Predicate<Type>() {
            public boolean matches(Type other) {
                return Types.matches(type, other);
            }
        };
    }


}
