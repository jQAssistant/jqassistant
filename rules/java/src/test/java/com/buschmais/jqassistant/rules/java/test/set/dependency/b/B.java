package com.buschmais.jqassistant.rules.java.test.set.dependency.b;

import com.buschmais.jqassistant.rules.java.test.set.dependency.a.A;

/**
 * A class depending on {@link com.buschmais.jqassistant.rules.java.test.set.dependency.a.A}.
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
