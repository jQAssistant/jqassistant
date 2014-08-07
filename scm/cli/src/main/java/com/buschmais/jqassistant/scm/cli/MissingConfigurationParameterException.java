package com.buschmais.jqassistant.scm.cli;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public class MissingConfigurationParameterException extends RuntimeException {
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 6731018529360115384L;

    public MissingConfigurationParameterException(final String message) {
        super(message);
    }
}
