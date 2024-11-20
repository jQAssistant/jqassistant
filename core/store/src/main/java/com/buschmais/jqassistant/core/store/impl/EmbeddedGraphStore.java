package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import com.buschmais.jqassistant.core.shared.aether.configuration.Plugin;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.embedded.impl.Neo4jCommunityServerFactory;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jDatastoreSession;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
@Slf4j
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedGraphStore.class);

    private static final int AUTOCOMMIT_THRESHOLD = 32678;
    public static final String NEO4J_PLUGIN_DIR_PREFIX = "jqassistant-neo4j-plugins";

    private final EmbeddedNeo4jServerFactory serverFactory;

    private final EmbeddedNeo4jServer embeddedNeo4jServer;

    private final Embedded embedded;

    private final ArtifactProvider artifactProvider;

    /**
     * Constructor.
     *
     * @param uri
     *     The store {@link URI}.
     * @param configuration
     *     The configuration.
     * @param storePluginRepository
     *     The {@link StorePluginRepository}.
     */
    public EmbeddedGraphStore(URI uri, com.buschmais.jqassistant.core.store.api.configuration.Store configuration, StorePluginRepository storePluginRepository,
        ArtifactProvider artifactProvider) {
        super(uri, configuration, storePluginRepository);
        this.serverFactory = getEmbeddedNeo4jServerFactory();
        this.embeddedNeo4jServer = serverFactory.getServer();
        this.embedded = configuration.embedded();
        this.artifactProvider = artifactProvider;
    }

    public EmbeddedNeo4jServer getEmbeddedNeo4jServer() {
        return this.embeddedNeo4jServer;
    }

    @Override
    protected XOUnit configure(XOUnit.XOUnitBuilder builder) {
        List<File> plugins = resolveNeo4jPlugins();
        Properties properties = serverFactory.getProperties(this.embedded.connectorEnabled(), this.embedded.listenAddress(), this.embedded.boltPort(),
            plugins);
        builder.properties(properties);
        builder.provider(EmbeddedNeo4jXOProvider.class);
        return builder.build();
    }

    private List<File> resolveNeo4jPlugins() {
        List<Plugin> plugins = embedded.neo4jPlugins();
        log.info("Resolving {} Neo4j plugin(s).", plugins.size());
        return artifactProvider.resolve(plugins);
    }

    @Override
    protected final void initialize(XOManagerFactory xoManagerFactory) {
        LOGGER.debug("Initializing embedded Neo4j server.");
        EmbeddedDatastore embeddedDatastore = (EmbeddedDatastore) xoManagerFactory.getDatastore(EmbeddedDatastore.class);
        embeddedNeo4jServer.initialize(embedded.listenAddress(), embedded.httpPort(), embedded.boltPort(), storePluginRepository.getClassLoader());
        logVersion(embeddedDatastore);
    }

    private void logVersion(EmbeddedDatastore embeddedDatastore) {
        try (EmbeddedNeo4jDatastoreSession session = embeddedDatastore.createSession()) {
            String neo4jVersion = session.getNeo4jVersion();
            LOGGER.info("Initialized embedded Neo4j database '{}'.", neo4jVersion);
        }
    }

    @Override
    protected void destroy() {
    }

    private EmbeddedNeo4jServerFactory getEmbeddedNeo4jServerFactory() {
        return new Neo4jCommunityServerFactory();
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
