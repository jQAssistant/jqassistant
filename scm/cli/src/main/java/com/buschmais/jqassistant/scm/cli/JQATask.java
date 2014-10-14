package com.buschmais.jqassistant.scm.cli;

import java.util.Map;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface JQATask extends Runnable, OptionsProvider, OptionsConsumer {

    void initialize(Map<String, Object> properties);

    String getName();

    void withGlobalOptions(CommandLine commandLine);
}
