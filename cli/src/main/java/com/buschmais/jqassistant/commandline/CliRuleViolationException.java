package com.buschmais.jqassistant.commandline;

/**
 * Indicates a violation of a rule.
 */
public class CliRuleViolationException extends CliExecutionException {

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public CliRuleViolationException(String message) {
        super(message);
    }

    @Override
    public int getExitCode() {
        return 2;
    }
}
