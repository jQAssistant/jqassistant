package com.buschmais.jqassistant.commandline.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;

public abstract class AbstractTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    protected PluginRepository pluginRepository;
    protected RuleHelper ruleHelper;
    protected ReportHelper reportHelper;
    protected Map<String, Object> pluginProperties;

    @Override
    public void initialize(PluginRepository pluginRepository, Map<String, Object> pluginProperties) {
        this.pluginRepository = pluginRepository;
        this.pluginProperties = pluginProperties;
        this.ruleHelper = new RuleHelper(LOGGER);
        this.reportHelper = new ReportHelper(LOGGER);
    }


    @Override
    public List<Option> getOptions() {
        return emptyList();
    }

    @Override
    public void withOptions(CommandLine options) throws CliConfigurationException {
    }

    @Override
    public void withStandardOptions(CommandLine commandLine) throws CliConfigurationException {
    }

    protected List<String> getOptionValues(CommandLine options, String option, List<String> defaultValues) {
        if (options.hasOption(option)) {
            List<String> names = new ArrayList<>();
            for (String elementName : options.getOptionValues(option)) {
                if (elementName.trim().length() > 0) {
                    names.add(elementName);
                }
            }
            return names;
        }
        return defaultValues;
    }

    protected String getOptionValue(CommandLine options, String option) {
        return getOptionValue(options, option, null);
    }

    protected String getOptionValue(CommandLine options, String option, String defaultValue) {
        if (options.hasOption(option)) {
            return options.getOptionValue(option);
        } else {
            return defaultValue;
        }
    }
}
