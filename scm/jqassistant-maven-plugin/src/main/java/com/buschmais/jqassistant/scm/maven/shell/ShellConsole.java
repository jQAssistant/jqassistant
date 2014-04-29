package com.buschmais.jqassistant.scm.maven.shell;

import java.rmi.RemoteException;

import org.neo4j.shell.Output;

import com.buschmais.jqassistant.core.analysis.api.Console;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.core.analysis.api.Console} delegating to the
 * neo4j shell output.
 */
class ShellConsole implements Console {

    private final Output out;

    public ShellConsole(Output out) {
        this.out = out;
    }

    @Override
    public void debug(String message) {
        try {
            out.println(message);
        } catch (RemoteException e) {
            throw new IllegalStateException("Cannot print error message.", e);
        }
    }

    @Override
    public void info(String message) {
        try {
            out.println(message);
        } catch (RemoteException e) {
            throw new IllegalStateException("Cannot print error message.", e);
        }
    }

    @Override
    public void warn(String message) {
        try {
            out.println(message);
        } catch (RemoteException e) {
            throw new IllegalStateException("Cannot print error message.", e);
        }
    }

    @Override
    public void error(String message) {
        try {
            out.println(message);
        } catch (RemoteException e) {
            throw new IllegalStateException("Cannot print error message.", e);
        }
    }
}
