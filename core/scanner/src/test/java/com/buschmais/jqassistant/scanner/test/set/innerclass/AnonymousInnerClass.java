package com.buschmais.jqassistant.scanner.test.set.innerclass;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 03.07.13
 * Time: 08:52
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousInnerClass<X> {

    public Iterator<X> iterator() {
        return new Iterator<X>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public X next() {
                return null;
            }

            @Override
            public void remove() {
            }
        };
    }
}
