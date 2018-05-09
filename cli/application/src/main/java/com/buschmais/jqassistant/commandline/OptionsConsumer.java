package com.buschmais.jqassistant.commandline;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsConsumer {
    void withOptions(final CommandLine options) throws com.buschmais.jqassistant.commandline.CliConfigurationException;
}
