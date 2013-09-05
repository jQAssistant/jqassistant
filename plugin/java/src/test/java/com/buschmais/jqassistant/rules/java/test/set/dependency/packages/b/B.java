package com.buschmais.jqassistant.rules.java.test.set.dependency.packages.b;

import com.buschmais.jqassistant.rules.java.test.set.dependency.packages.a.A;

/**
 * A class depending on {@link A}.
 */
public class B {

    private A a;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }
}
