package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractStoreTask extends AbstractTask {

    protected static final String CMDLINE_OPTION_STORE_URI = "storeUri";
    protected static final String CMDLINE_OPTION_STORE_USERNAME = "storeUsername";
    protected static final String CMDLINE_OPTION_STORE_PASSWORD = "storePassword";
    @Deprecated
    protected static final String CMDLINE_OPTION_S = "s";
    @Deprecated
    protected static final String CMDLINE_OPTION_STORE_DIRECTORY = "storeDirectory";


    protected StoreConfiguration storeConfiguration;

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
    public final void withStandardOptions(CommandLine options) throws CliConfigurationException {
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

    protected abstract void addTaskOptions(List<Option> options);

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
