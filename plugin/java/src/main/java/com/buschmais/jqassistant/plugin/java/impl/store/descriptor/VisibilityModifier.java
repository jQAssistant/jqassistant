/**
 *
 */
package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

/**
 * Enum of all access modifiers.
 * 
 * @author Herklotz
 */
public enum VisibilityModifier {

    /**
     * Modifier: private.
     */
    PRIVATE,
    /**
     * Modifier: default.
     */
    DEFAULT,
    /**
     * Modifier: protected.
     */
    PROTECTED,
    /**
     * Modifier: public.
     */
    PUBLIC;

    /**
     * Return the value to be stored.
     *
     * @return The value.
     */
    public String getValue() {
        return name().toLowerCase();
    }

}
