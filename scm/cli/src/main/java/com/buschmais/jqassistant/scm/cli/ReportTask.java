package com.buschmais.jqassistant.scm.cli;

import org.apache.commons.cli.CommandLine;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.store.api.Store;

public class ReportTask extends AbstractJQATask {

    private String reportDirectory;

    public ReportTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    protected void executeTask(final Store store) {
    }

    @Override
    public void withOptions(CommandLine options) {
        reportDirectory = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
    }
}
