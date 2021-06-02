package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

/**
 * Provides structures for type parameters on outer and inner classes.
 *
 * @param <X>
 * @param <Y>
 */
public class GenericTypeDeclarations<X, Y> {

    private X x;

    private Y y;

    public class Inner<X> extends GenericTypeDeclarations<X, Y> {

        /**
         * References the inner class parameter
         */
        private X x;

        /**
         * References the outer class parameter, will be required
         */
        private Y y;

    }

}
