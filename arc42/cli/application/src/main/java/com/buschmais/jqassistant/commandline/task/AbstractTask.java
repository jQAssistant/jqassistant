package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public abstract class AbstractTask implements Task {

    protected File outputDirectory;
    protected StoreFactory storeFactory;

    protected PluginRepository pluginRepository;
    protected RuleHelper ruleHelper;

    @Override
    public final void initialize(PluginRepository pluginRepository, StoreFactory storeFactory) {
        this.outputDirectory = new File(DEFAULT_OUTPUT_DIRECTORY);
        this.pluginRepository = pluginRepository;
        this.storeFactory = storeFactory;
        this.ruleHelper = new RuleHelper();
    }

    @Override
    public final List<Option> getOptions() {
        List<Option> options = new ArrayList<>();
        addTaskOptions(options);
        return options;
    }

    protected List<String> getOptionValues(CommandLine options, String option, List<String> defaultValues) {
        if (options.hasOption(option)) {
            List<String> names = new ArrayList<>();
            for (String elementName : options.getOptionValues(option)) {
                if (elementName.trim()
                    .length() > 0) {
                    names.add(elementName);
                }
            }
            return names;
        }
        return defaultValues;
    }

    protected abstract void addTaskOptions(List<Option> options);
}
