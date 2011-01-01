package com.googlecode.yadic.examples;

public class AlternativeUserRepository implements UserRepository{
    public User get(int id) {
        throw new UnsupportedOperationException();
    }
}
