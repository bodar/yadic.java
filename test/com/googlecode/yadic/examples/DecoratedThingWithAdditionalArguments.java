package com.googlecode.yadic.examples;

public class DecoratedThingWithAdditionalArguments implements Thing {
    private final Thing dependency;

    public DecoratedThingWithAdditionalArguments(Thing dependency, String additionalArgument) {
        this.dependency = dependency;
    }

    public Thing dependency() {
        return dependency;
    }
}
