package com.googlecode.yadic.examples;

public class SelfReferencingClass {
    private SelfReferencingClass() {
    }

    public static SelfReferencingClass myStaticMethodClass(SelfReferencingClass self) {
        throw new AssertionError();
    }

    public static SelfReferencingClass myStaticMethodClass(SelfReferencingClass self, SelfReferencingClass anotherSelf) {
        throw new AssertionError();
    }
}
