package com.googlecode.yadic.examples;

public class MyThingWithReverseConstructor implements Thing {
    private final ThingWithNoDependencies dependency;

    public MyThingWithReverseConstructor(ThingWithNoDependencies dependency) {

        this.dependency = dependency;
    }

    public MyThingWithReverseConstructor() {
        this(null);
    }

    public Thing dependency() {
        return dependency;
    }


}
