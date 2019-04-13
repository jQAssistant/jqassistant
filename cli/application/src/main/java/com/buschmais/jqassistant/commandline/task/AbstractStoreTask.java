package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.shared.option.OptionHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;

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
    protected static final String CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS = "embeddedListenAddress";
    protected static final String CMDLINE_OPTION_EMBEDDED_BOLT_PORT = "embeddedBoltPort";
    protected static final String CMDLINE_OPTION_EMBEDDED_HTTP_PORT = "embeddedHttpPort";
    protected static final String CMDLINE_OPTION_EMBEDDED_APOC_ENABLED = "embeddedApocEnabled";
    protected static final String CMDLINE_OPTION_EMBEDDED_GRAPH_ALGORITHMS_ENABLED = "embeddedGraphAlgorithmsEnabled";

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
            String directoryName = OptionHelper.selectValue(DEFAULT_STORE_DIRECTORY, storeDirectory);
            File directory = new File(directoryName);
            directory.getParentFile().mkdirs();
            builder.uri(directory.toURI());
        }
        builder.embedded(getEmbeddedNeo4jConfiguration(options));
        this.storeConfiguration = builder.build();
    }

    private EmbeddedNeo4jConfiguration getEmbeddedNeo4jConfiguration(CommandLine options) {
        EmbeddedNeo4jConfiguration.EmbeddedNeo4jConfigurationBuilder builder = EmbeddedNeo4jConfiguration.builder();
        builder.connectorEnabled(isConnectorRequired());

        String embeddedListenAddress = getOptionValue(options, CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS);
        builder.listenAddress(OptionHelper.selectValue(EmbeddedNeo4jConfiguration.DEFAULT_LISTEN_ADDRESS, embeddedListenAddress));

        String httpPort = getOptionValue(options, CMDLINE_OPTION_EMBEDDED_HTTP_PORT);
        builder.httpPort(Integer.valueOf(OptionHelper.selectValue(Integer.toString(EmbeddedNeo4jConfiguration.DEFAULT_HTTP_PORT), httpPort)));

        String boltPort = getOptionValue(options, CMDLINE_OPTION_EMBEDDED_BOLT_PORT);
        builder.boltPort(Integer.valueOf(OptionHelper.selectValue(Integer.toString(EmbeddedNeo4jConfiguration.DEFAULT_BOLT_PORT), boltPort)));

        String apocEnabled = getOptionValue(options, CMDLINE_OPTION_EMBEDDED_APOC_ENABLED, Boolean.toString(EmbeddedNeo4jConfiguration.DEFAULT_APOC_ENABLED));
        builder.apocEnabled(Boolean.valueOf(apocEnabled.toLowerCase()));

        String graphAlgorithmsEnabled = getOptionValue(options, CMDLINE_OPTION_EMBEDDED_GRAPH_ALGORITHMS_ENABLED, Boolean.toString(EmbeddedNeo4jConfiguration.DEFAULT_GRAPH_ALGORITHMS_ENABLED));
        builder.graphAlgorithmsEnabled(Boolean.valueOf(graphAlgorithmsEnabled.toLowerCase()));

        return builder.build();
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
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS).withDescription("The listen address of the embedded server.").hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_HTTP_PORT).withDescription("The HTTP port of the embedded server.").hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_HTTP_PORT));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_BOLT_PORT).withDescription("The Bolt tport of the embedded server.").hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_BOLT_PORT));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_APOC_ENABLED).withDescription("Activate/deactivate registration of APOC user functions and procedures in the embedded server.").hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_APOC_ENABLED));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_GRAPH_ALGORITHMS_ENABLED)
                .withDescription("Activate/deactivate registration of graph algorithm procedures in the embedded server.").hasArgs()
                .create(CMDLINE_OPTION_EMBEDDED_GRAPH_ALGORITHMS_ENABLED));
        addTaskOptions(options);
        return options;
    }

    /**
     * Return the {@link Store} instance.
     *
     * @return The store.
     */
    protected Store getStore() {
        return StoreFactory.getStore(storeConfiguration);
    }

    protected abstract void addTaskOptions(List<Option> options);

    protected abstract boolean isConnectorRequired();

    protected abstract void executeTask(final Store store) throws CliExecutionException;

}
