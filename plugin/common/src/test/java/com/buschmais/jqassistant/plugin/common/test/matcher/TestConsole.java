package com.buschmais.jqassistant.plugin.common.test.matcher;

import com.buschmais.jqassistant.core.analysis.api.Console;

/**
 * Created by Dirk Mahler on 29.04.2014.
 */
public class TestConsole implements Console {

    private void print(String message) {
        System.out.println(message);
    }

    @Override
    public void debug(String message) {
        print(message);
    }

    @Override
    public void info(String message) {
        print(message);
    }

    @Override
    public void warn(String message) {
        print(message);
    }

    @Override
    public void error(String message) {
        print(message);
    }
}
