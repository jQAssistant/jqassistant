package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.logging.Log;

import com.buschmais.jqassistant.core.analysis.api.Console;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.core.analysis.api.Console} delegating to the maven logger.
 */
public class MavenConsole implements Console {

    private Log log;

    public MavenConsole(Log log) {
        this.log = log;
    }

    @Override
    public void debug(String message) {
        log.debug(message);
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void error(String message) {
        log.error(message);
    }
}
