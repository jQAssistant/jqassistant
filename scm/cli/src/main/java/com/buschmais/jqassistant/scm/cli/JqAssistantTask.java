package com.buschmais.jqassistant.scm.cli;

import java.util.Properties;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface JqAssistantTask extends Runnable, OptionsProvider {
    void initialize(Properties properties);

    String getName();
}
