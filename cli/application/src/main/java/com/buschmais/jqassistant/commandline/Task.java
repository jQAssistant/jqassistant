package com.buschmais.jqassistant.commandline;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;

import org.apache.commons.cli.Options;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface Task extends OptionsProvider, OptionsConsumer {

    String DEFAULT_OUTPUT_DIRECTORY = "jqassistant";
    String DEFAULT_STORE_DIRECTORY = "jqassistant/store";
    String DEFAULT_RULE_DIRECTORY = "jqassistant/rules";
    String DEFAULT_REPORT_DIRECTORY = "jqassistant/report";
    String REPORT_FILE_XML = "jqassistant-report.xml";

    void initialize(PluginRepository pluginRepository) throws CliExecutionException;

    void run(CliConfiguration configuration, Options options) throws CliExecutionException;
}
