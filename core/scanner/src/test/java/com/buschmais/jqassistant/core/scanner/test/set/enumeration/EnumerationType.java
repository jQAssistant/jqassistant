package com.buschmais.jqassistant.core.scanner.test.set.enumeration;

/**
 * An enum type.
 */
public enum EnumerationType {

    A(true),
    B(false);

    private boolean value;

    EnumerationType(boolean value) {
        this.value = value;
    }


}
