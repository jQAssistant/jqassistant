package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractJQATask implements JQATask {

    protected final String taskName;
    protected Map<String, Object> properties;
    protected PluginConfigurationReader pluginConfigurationReader;
    protected String storeDirectory = "./tmp/jQAssistant/store";

    protected AbstractJQATask(final String taskName) {
        this.taskName = taskName;
        this.pluginConfigurationReader = new PluginConfigurationReaderImpl();
    }

    @Override
    public void initialize(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String getName() {
        return taskName;
    }

    protected Store getStore() {
        File directory = new File(storeDirectory);
        Log.getLog().info("Opening store in directory '" + directory.getAbsolutePath() + "'");
        directory.getParentFile().mkdirs();
        return new EmbeddedGraphStore(directory.getAbsolutePath());
    }

    @Override
    public void run() {
        List<Class<?>> descriptorTypes;
        final Store store = getStore();

        try {
            descriptorTypes = getModelPluginRepository().getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot get descriptor mappers.", e);
        }

        try {
            store.start(descriptorTypes);
            executeTask(store);
        } finally {
            store.stop();
        }
    }

    @Override
    public void withGlobalOptions(CommandLine options) {
        if (options.hasOption("s")) {
            storeDirectory = options.getOptionValue("s");
        }
        if (storeDirectory.isEmpty()) {
            throw new MissingConfigurationParameterException("Invalid store directory.");
        }
    }

    protected ModelPluginRepository getModelPluginRepository() {
        try {
            return new ModelPluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot create model plugin repository.", e);
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository(Map<String, Object> properties) {
        try {
            return new ScannerPluginRepositoryImpl(pluginConfigurationReader, properties);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }

    protected RulePluginRepository getRulePluginRepository() {
        try {
            return new RulePluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        options.add(OptionBuilder.withArgName("s").withLongOpt("storeDirectory").withDescription("The location of the Neo4j database").withValueSeparator(',')
                .hasArgs().create("d"));
        addTaskOptions(options);
        return options;
    }

    protected void addTaskOptions(final List<Option> options) {
    }

    protected abstract void executeTask(final Store store);
}
