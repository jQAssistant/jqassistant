package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.io.Serializable;
import java.util.List;

/**
 * Provides structures for type parameters on outer and inner classes.
 */
public class GenericTypeDeclarations<X, Y extends Serializable & List<String>> {

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
