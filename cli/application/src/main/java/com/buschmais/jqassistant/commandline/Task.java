package com.buschmais.jqassistant.commandline;

import java.util.Map;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface Task extends OptionsProvider, OptionsConsumer {

    String DEFAULT_OUTPUT_DIRECTORY = "jqassistant";
    String DEFAULT_STORE_DIRECTORY = "jqassistant/store";
    String DEFAULT_RULE_DIRECTORY = "jqassistant/rules";
    String DEFAULT_REPORT_DIRECTORY = "jqassistant/report";

    String CMDLINE_OPTION_REPORTDIR = "reportDirectory";

    String REPORT_FILE_XML = "jqassistant-report.xml";

    void initialize(PluginRepository pluginRepository, Map<String, Object> pluginProperties) throws com.buschmais.jqassistant.commandline.CliExecutionException;

    void run(CliConfiguration configuration) throws com.buschmais.jqassistant.commandline.CliExecutionException;
}
