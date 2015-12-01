package com.buschmais.jqassistant.plugin.common.api.model;

/**
 * Template for descriptor which indicate that information is invalid.
 */
public interface ValidDescriptor {

    boolean isValid();

    void setValid(boolean valid);

}
