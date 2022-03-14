package com.buschmais.jqassistant.commandline;

import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsConsumer {

    void withStandardOptions(CommandLine commandLine, ConfigurationBuilder configurationBuilder) throws CliConfigurationException;

    void withOptions(final CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException;

}
