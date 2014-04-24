package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.logging.Log;

import com.buschmais.jqassistant.scm.common.Console;

/**
 * Created by Dirk Mahler on 22.04.2014.
 */
public class MavenConsole implements Console {

    private Log log;

    public MavenConsole(Log log) {
        this.log = log;
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
