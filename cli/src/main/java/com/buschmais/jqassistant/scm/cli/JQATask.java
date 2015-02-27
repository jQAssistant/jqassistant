package com.buschmais.jqassistant.scm.cli;

import java.util.Map;

import org.apache.commons.cli.CommandLine;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface JQATask extends OptionsProvider, OptionsConsumer {

    public static final String DEFAULT_STORE_DIRECTORY = "jqassistant/store";
    public static final String DEFAULT_RULE_DIRECTORY = "jqassistant/rules";
    public static final String DEFAULT_REPORT_DIRECTORY = "jqassistant/report";

    public static final String REPORT_FILE_XML = "jqassistant-report.xml";

    void initialize(PluginConfigurationReader pluginConfigurationReader, Map<String, Object> properties) throws CliExecutionException;

    void withStandardOptions(CommandLine commandLine);

    void run() throws CliExecutionException;
}
