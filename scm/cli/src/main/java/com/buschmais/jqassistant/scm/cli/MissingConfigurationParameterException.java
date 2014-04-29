package com.buschmais.jqassistant.scm.cli;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public class MissingConfigurationParameterException extends RuntimeException {
    public MissingConfigurationParameterException(final String message) {
        super(message);
    }
}
