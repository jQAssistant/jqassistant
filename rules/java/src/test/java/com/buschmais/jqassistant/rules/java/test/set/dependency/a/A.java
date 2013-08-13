package com.buschmais.jqassistant.rules.java.test.set.dependency.a;

import com.buschmais.jqassistant.rules.java.test.set.dependency.b.B;

/**
 * A class depending on {@link com.buschmais.jqassistant.rules.java.test.set.dependency.b.B}.
 */
public class A {

    private B b;

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}
