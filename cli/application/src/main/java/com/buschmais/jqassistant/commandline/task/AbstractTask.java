package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Task {

    private static final String CMDLINE_OPTION_CONFIG_LOCATIONS = "configurationLocations";

    private static final String CMDLINE_OPTION_SKIP = "skip";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    protected File outputDirectory;
    protected PluginRepository pluginRepository;
    protected RuleHelper ruleHelper;
    protected Map<String, Object> pluginProperties;

    @Override
    public void initialize(PluginRepository pluginRepository, Map<String, Object> pluginProperties) {
        this.outputDirectory = new File(DEFAULT_OUTPUT_DIRECTORY);
        this.pluginRepository = pluginRepository;
        this.pluginProperties = pluginProperties;
        this.ruleHelper = new RuleHelper(LOGGER);
    }

    @Override
    public final List<Option> getOptions() {
        List<Option> options = new ArrayList<>();
        options.add(Option.builder()
            .argName(CMDLINE_OPTION_SKIP)
            .longOpt(CMDLINE_OPTION_SKIP)
            .desc("Skip execution.")
            .build());
        addTaskOptions(options);
        return options;
    }

    @Override
    public void configure(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        configurationBuilder.with(CliConfiguration.class, CliConfiguration.SKIP, getOptionValue(options, CMDLINE_OPTION_SKIP));

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

    protected abstract void addTaskOptions(List<Option> options);

}
