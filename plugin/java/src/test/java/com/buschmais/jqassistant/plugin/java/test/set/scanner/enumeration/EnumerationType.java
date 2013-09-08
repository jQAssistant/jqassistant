package com.buschmais.jqassistant.plugin.java.test.set.scanner.enumeration;

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
