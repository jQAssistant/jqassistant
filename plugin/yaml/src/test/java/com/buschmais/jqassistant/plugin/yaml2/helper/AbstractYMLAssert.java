package com.buschmais.jqassistant.plugin.yaml2.helper;

import org.assertj.core.api.AbstractObjectAssert;

abstract class AbstractYMLAssert<SELF extends AbstractObjectAssert<SELF, ACTUAL>, ACTUAL>
    extends AbstractObjectAssert<SELF, ACTUAL> {

    AbstractYMLAssert(ACTUAL actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public abstract SELF andContinueAssertionOnThis();

}
