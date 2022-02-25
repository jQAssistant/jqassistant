package com.buschmais.jqassistant.core.store.impl;

import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServerFactory;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedGraphStore.class);

    private static final int AUTOCOMMIT_THRESHOLD = 32678;

    private EmbeddedNeo4jServerFactory serverFactory;

    private EmbeddedNeo4jServer server;

    private EmbeddedNeo4jConfiguration embeddedNeo4jConfiguration;

    /**
     * Constructor.
     *
     * @param configuration
     *     The configuration.
     */
    public EmbeddedGraphStore(StoreConfiguration configuration, StorePluginRepository storePluginRepository) {
        super(configuration, storePluginRepository);
        this.serverFactory = getEmbeddedNeo4jServerFactory();
    }

    public EmbeddedNeo4jServer getServer() {
        return this.server;
    }

    @Override
    protected XOUnit configure(XOUnit.XOUnitBuilder builder, StoreConfiguration storeConfiguration) {
        this.embeddedNeo4jConfiguration = storeConfiguration.getEmbedded();
        // Determine store specific default properties
        Properties properties = serverFactory.getProperties(this.embeddedNeo4jConfiguration);
        // Add/overwrite with user properties
        properties.putAll(storeConfiguration.getProperties());
        builder.properties(properties);
        builder.provider(EmbeddedNeo4jXOProvider.class);
        return builder.build();
    }

    @Override
    protected void initialize(XOManagerFactory xoManagerFactory) {
        this.server = serverFactory.getServer();
        LOGGER.info("Initializing embedded Neo4j server " + server.getVersion());
        EmbeddedDatastore embeddedDatastore = (EmbeddedDatastore) xoManagerFactory.getDatastore(EmbeddedDatastore.class);
        server.initialize(embeddedDatastore, embeddedNeo4jConfiguration, storePluginRepository.getClassLoader(), storePluginRepository.getProcedureTypes(),
            storePluginRepository.getFunctionTypes());
    }

    private EmbeddedNeo4jServerFactory getEmbeddedNeo4jServerFactory() {
        ServiceLoader<EmbeddedNeo4jServerFactory> serverFactories = ServiceLoader.load(EmbeddedNeo4jServerFactory.class);
        Iterator<EmbeddedNeo4jServerFactory> iterator = serverFactories.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new IllegalStateException("Cannot find server factory.");
        }
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
