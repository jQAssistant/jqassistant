package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractTask implements Task {

    protected static final String CMDLINE_OPTION_STORE_URI = "storeUri";
    protected static final String CMDLINE_OPTION_STORE_USERNAME = "storeUsername";
    protected static final String CMDLINE_OPTION_STORE_PASSWORD = "storePassword";
    @Deprecated
    protected static final String CMDLINE_OPTION_S = "s";
    @Deprecated
    protected static final String CMDLINE_OPTION_STORE_DIRECTORY = "storeDirectory";
    protected static final String CMDLINE_OPTION_REPORTDIR = "reportDirectory";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    protected StoreConfiguration storeConfiguration;
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
    public void withStandardOptions(CommandLine options) throws CliConfigurationException {
        StoreConfiguration.StoreConfigurationBuilder builder = StoreConfiguration.builder();
        String storeUri = getOptionValue(options, CMDLINE_OPTION_STORE_URI);
        String storeDirectory = getOptionValue(options, CMDLINE_OPTION_S);
        if (storeUri != null && storeDirectory != null) {
            throw new CliConfigurationException("Expecting either parameter '" + CMDLINE_OPTION_STORE_DIRECTORY + "' or '" + CMDLINE_OPTION_STORE_URI + "'.");
        }
        if (storeUri != null) {
            try {
                builder.uri(new URI(storeUri));
            } catch (URISyntaxException e) {
                throw new CliConfigurationException("Cannot parse URI " + storeUri, e);
            }
            builder.username(getOptionValue(options, CMDLINE_OPTION_STORE_USERNAME));
            builder.password(getOptionValue(options, CMDLINE_OPTION_STORE_PASSWORD));
        } else {
            File directory;
            if (storeDirectory != null) {
                directory = new File(storeDirectory);
            } else {
                directory = new File(DEFAULT_STORE_DIRECTORY);
            }
            directory.getParentFile().mkdirs();
            builder.uri(directory.toURI());
        }
        this.storeConfiguration = builder.build();
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_S).withLongOpt(CMDLINE_OPTION_STORE_DIRECTORY)
                .withDescription("The location of the Neo4j database. Deprecated, use '" + CMDLINE_OPTION_STORE_URI + "' instead.").hasArgs()
                .create(CMDLINE_OPTION_S));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_URI)
                .withDescription("The URI of the Neo4j database, e.g. 'file:jqassistant/store' or 'bolt://localhost:7687'.").hasArgs()
                .create(CMDLINE_OPTION_STORE_URI));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_USERNAME).withDescription("The user name for connecting to Neo4j database.").hasArgs()
                .create(CMDLINE_OPTION_STORE_USERNAME));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_PASSWORD).withDescription("The password for connecting to Neo4j database.").hasArgs()
                .create(CMDLINE_OPTION_STORE_PASSWORD));
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

    protected void addTaskOptions(final List<Option> options) {
    }

    /**
     * Return the {@link Store} instance.
     *
     * @return The store.
     */
    protected Store getStore() {
        return StoreFactory.getStore(storeConfiguration);
    }

    protected abstract void executeTask(final Store store) throws CliExecutionException;
}
