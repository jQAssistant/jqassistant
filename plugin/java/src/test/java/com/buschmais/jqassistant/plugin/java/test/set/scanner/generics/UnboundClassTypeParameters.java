package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

public class UnboundClassTypeParameters<X, Y> {

    private class Inner {


        private class InnerInner {

            X field;

        }

    }

}
