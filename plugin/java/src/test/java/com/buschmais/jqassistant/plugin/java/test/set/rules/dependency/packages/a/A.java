package com.buschmais.jqassistant.plugin.java.test.set.dependency.packages.a;

import com.buschmais.jqassistant.plugin.java.test.set.dependency.packages.b.B;

/**
 * A class depending on {@link B}.
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
