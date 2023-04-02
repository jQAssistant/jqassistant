package com.buschmais.jqassistant.commandline;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsConsumer {

    default void configure(final CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {};

}
