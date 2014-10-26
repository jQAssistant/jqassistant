package com.buschmais.jqassistant.scm.cli;

/**
 * Base class for CLI exceptions.
 */
public interface CliException {

    /**
     * Return the exit code to use for {@link java.lang.System#exit(int)}.
     * 
     * @return The exit code.
     */
    int getExitCode();
}
