package com.buschmais.jqassistant.commandline;

import java.util.Map;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsConsumer {

    void withStandardOptions(CommandLine commandLine, Map<String, String> configurationProperties) throws CliConfigurationException;

    void withOptions(final CommandLine options, Map<String, String> configurationProperties) throws CliConfigurationException;

}
