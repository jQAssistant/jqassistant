package com.buschmais.jqassistant.examples.rules.asciidoc.impl;

import com.buschmais.jqassistant.examples.rules.asciidoc.api.Service;

/**
 * A service implementation.
 */
public class ServiceImpl implements Service {

    @Override
    public int add(int... values) {
        int sum = 0;
        for (int value : values) {
            sum = +value;
        }
        return sum;
    }

}
