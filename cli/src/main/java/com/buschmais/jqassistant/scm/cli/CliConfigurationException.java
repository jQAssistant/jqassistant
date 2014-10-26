package com.buschmais.jqassistant.scm.cli;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public class CliConfigurationException extends CliExecutionException {
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 6731018529360115384L;

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public CliConfigurationException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message
     *            The message.
     * @param cause
     *            The cause.
     */
    public CliConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getExitCode() {
        return 1;
    }
}
