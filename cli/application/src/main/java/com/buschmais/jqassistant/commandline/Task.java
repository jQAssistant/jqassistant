package com.buschmais.jqassistant.commandline;

import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface Task extends com.buschmais.jqassistant.commandline.OptionsProvider, com.buschmais.jqassistant.commandline.OptionsConsumer {

    String DEFAULT_STORE_DIRECTORY = "jqassistant/store";
    String DEFAULT_RULE_DIRECTORY = "jqassistant/rules";
    String DEFAULT_REPORT_DIRECTORY = "jqassistant/report";

    String REPORT_FILE_XML = "jqassistant-report.xml";

    void initialize(PluginRepository pluginRepository, Map<String, Object> pluginProperties) throws com.buschmais.jqassistant.commandline.CliExecutionException;

    void withStandardOptions(CommandLine commandLine) throws CliConfigurationException;

    void run() throws com.buschmais.jqassistant.commandline.CliExecutionException;
}
