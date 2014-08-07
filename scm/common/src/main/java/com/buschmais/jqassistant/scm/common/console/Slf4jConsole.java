package com.buschmais.jqassistant.scm.common.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.Console;

/**
 * {@link com.buschmais.jqassistant.core.analysis.api.Console} implementation
 * which delegates to SLF4j.
 */
public class Slf4jConsole implements Console {

    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jConsole.class);

    @Override
    public void debug(String s) {
        LOGGER.debug(s);
    }

    @Override
    public void info(String s) {
        LOGGER.info(s);
    }

    @Override
    public void warn(String s) {
        LOGGER.warn(s);
    }

    @Override
    public void error(String s) {
        LOGGER.error(s);
    }
}
