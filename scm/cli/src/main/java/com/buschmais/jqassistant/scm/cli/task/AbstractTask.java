package com.buschmais.jqassistant.scm.cli.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.Task;
import com.buschmais.jqassistant.scm.common.report.RuleHelper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractTask implements Task {

    protected static final String CMDLINE_OPTION_S = "s";
    protected static final String CMDLINE_OPTION_STOREDIRECTORY = "storeDirectory";
    protected static final String CMDLINE_OPTION_REPORTDIR = "reportDirectory";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    protected String storeDirectory;
    protected PluginRepository pluginRepository;
    protected RuleHelper ruleHelper;
    protected ReportHelper reportHelper;
    protected Map<String, Object> pluginProperties;

    @Override
    public void initialize(PluginRepository pluginRepository, Map<String, Object> pluginProperties) throws CliExecutionException {
        this.pluginRepository = pluginRepository;
        this.pluginProperties = pluginProperties;
        this.ruleHelper = new RuleHelper(LOGGER);
        this.reportHelper = new ReportHelper(LOGGER);
    }

    @Override
    public void run() throws CliExecutionException {
        List<Class<?>> descriptorTypes;
        final Store store = getStore();
        try {
            descriptorTypes = pluginRepository.getModelPluginRepository().getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get model.", e);
        }
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(pluginRepository.getClassLoader());
        try {
            store.start(descriptorTypes);
            executeTask(store);
        } finally {
            store.stop();
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    @Override
    public void withStandardOptions(CommandLine options) {
        storeDirectory = getOptionValue(options, CMDLINE_OPTION_S, DEFAULT_STORE_DIRECTORY);
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_S).withLongOpt(CMDLINE_OPTION_STOREDIRECTORY)
                .withDescription("The location of the Neo4j database.").hasArgs().create(CMDLINE_OPTION_S));
        addTaskOptions(options);
        return options;
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

    protected String getOptionValue(CommandLine options, String option, String defaultValue) {
        if (options.hasOption(option)) {
            return options.getOptionValue(option);
        } else {
            return defaultValue;
        }
    }

    protected void addTaskOptions(final List<Option> options) {
    }

    /**
     * Return the {@link Store} instance.
     *
     * @return The store.
     */
    protected Store getStore() {
        File directory = new File(storeDirectory);
        LOGGER.info("Opening store in directory '" + directory.getAbsolutePath() + "'");
        if (!directory.exists()) {
            directory.getParentFile().mkdirs();
        }
        return new EmbeddedGraphStore(directory.getAbsolutePath());
    }

    protected abstract void executeTask(final Store store) throws CliExecutionException;
}
