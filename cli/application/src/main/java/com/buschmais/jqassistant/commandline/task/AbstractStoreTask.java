package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.configuration.Embedded;

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
    protected static final String CMDLINE_OPTION_STORE_ENCRYPTION = "storeEncryption";
    protected static final String CMDLINE_OPTION_STORE_TRUST_STRATEGY = "storeTrustStrategy";
    protected static final String CMDLINE_OPTION_STORE_TRUST_CERITFICATE = "storeTrustCertificate";
    protected static final String CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS = "embeddedListenAddress";
    protected static final String CMDLINE_OPTION_EMBEDDED_BOLT_PORT = "embeddedBoltPort";
    protected static final String CMDLINE_OPTION_EMBEDDED_HTTP_PORT = "embeddedHttpPort";

    @Deprecated
    protected static final String CMDLINE_OPTION_S = "s";
    @Deprecated
    protected static final String CMDLINE_OPTION_STORE_DIRECTORY = "storeDirectory";

    @Override
    public void run(CliConfiguration configuration) throws CliExecutionException {
        final Store store = getStore(configuration);
        try {
            store.start();
            executeTask(configuration, store);
        } finally {
            store.stop();
        }
    }

    @Override
    public final void withStandardOptions(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        String storeUri = getOptionValue(options, CMDLINE_OPTION_STORE_URI);
        String storeDirectory = getOptionValue(options, CMDLINE_OPTION_S);
        if (storeUri != null && storeDirectory != null) {
            throw new CliConfigurationException("Expecting either parameter '" + CMDLINE_OPTION_STORE_DIRECTORY + "' or '" + CMDLINE_OPTION_STORE_URI + "'.");
        }
        if (storeUri != null) {
            URI uri;
            try {
                uri = new URI(storeUri);
            } catch (URISyntaxException e) {
                throw new CliConfigurationException("Cannot parse URI " + storeUri, e);
            }
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.URI, uri);
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.USERNAME, getOptionValue(options, CMDLINE_OPTION_STORE_USERNAME));
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.PASSWORD, getOptionValue(options, CMDLINE_OPTION_STORE_PASSWORD));
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.ENCRYPTION, getOptionValue(options, CMDLINE_OPTION_STORE_ENCRYPTION));
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.TRUST_STRATEGY, getOptionValue(options, CMDLINE_OPTION_STORE_TRUST_STRATEGY));
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.TRUST_CERTIFICATE,
                getOptionValue(options, CMDLINE_OPTION_STORE_TRUST_CERITFICATE));
        } else if (storeDirectory != null) {
            File directory = new File(storeDirectory);
            directory.getParentFile()
                .mkdirs();
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.URI, directory.toURI());
        }
        configurationBuilder.with(Embedded.class, Embedded.CONNECTORY_ENABLED, isConnectorRequired());
        configurationBuilder.with(Embedded.class, Embedded.LISTEN_ADDRESS, getOptionValue(options, CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS));
        configurationBuilder.with(Embedded.class, Embedded.HTTP_PORT, getOptionValue(options, CMDLINE_OPTION_EMBEDDED_HTTP_PORT));
        configurationBuilder.with(Embedded.class, Embedded.BOLT_PORT, getOptionValue(options, CMDLINE_OPTION_EMBEDDED_BOLT_PORT));
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_S)
            .withLongOpt(CMDLINE_OPTION_STORE_DIRECTORY)
            .withDescription("The location of the Neo4j database. Deprecated, use '" + CMDLINE_OPTION_STORE_URI + "' instead.")
            .hasArgs()
            .create(CMDLINE_OPTION_S));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_URI)
            .withDescription("The URI of the Neo4j database, e.g. 'file:jqassistant/store' or 'bolt://localhost:7687'.")
            .hasArgs()
            .create(CMDLINE_OPTION_STORE_URI));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_USERNAME)
            .withDescription("The user name for bolt connections.")
            .hasArgs()
            .create(CMDLINE_OPTION_STORE_USERNAME));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_PASSWORD)
            .withDescription("The password for bolt connections.")
            .hasArgs()
            .create(CMDLINE_OPTION_STORE_PASSWORD));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_ENCRYPTION)
            .withDescription("The encryption level for bolt connections, may be true or false (default).")
            .hasArgs()
            .create(CMDLINE_OPTION_STORE_ENCRYPTION));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_TRUST_STRATEGY)
            .withDescription(
                "The trust strategy for bolt connections, may be trustAllCertificates, trustCustomCaSignedCertificates or trustSystemCaSignedCertificates.")
            .hasArgs()
            .create(CMDLINE_OPTION_STORE_TRUST_STRATEGY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_STORE_TRUST_CERITFICATE)
            .withDescription("The file containing the custom CA certificate for trust strategy trustCustomCaSignedCertificates.")
            .hasArgs()
            .create(CMDLINE_OPTION_STORE_TRUST_CERITFICATE));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS)
            .withDescription("The listen address of the embedded server.")
            .hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_LISTEN_ADDRESS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_HTTP_PORT)
            .withDescription("The HTTP port of the embedded server.")
            .hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_HTTP_PORT));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_EMBEDDED_BOLT_PORT)
            .withDescription("The Bolt tport of the embedded server.")
            .hasArgs()
            .create(CMDLINE_OPTION_EMBEDDED_BOLT_PORT));
        addTaskOptions(options);
        return options;
    }

    /**
     * Return the {@link Store} instance.
     *
     * @return The store.
     */
    protected Store getStore(CliConfiguration configuration) {
        return StoreFactory.getStore(configuration.store(), () -> new File(DEFAULT_STORE_DIRECTORY), pluginRepository.getStorePluginRepository());
    }

    protected abstract void addTaskOptions(List<Option> options);

    protected abstract boolean isConnectorRequired();

    protected abstract void executeTask(CliConfiguration configuration, final Store store) throws CliExecutionException;

}
