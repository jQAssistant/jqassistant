package com.buschmais.jqassistant.commandline;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public class CliExecutionException extends Exception implements CliException {

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
    public CliExecutionException(final String message) {
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
    public CliExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getExitCode() {
        return 1;
    }
}
