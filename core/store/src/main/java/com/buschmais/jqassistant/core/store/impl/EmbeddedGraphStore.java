package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.buschmais.jqassistant.core.shared.aether.configuration.Exclusion;
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

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
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
    protected void configure(XOUnit.XOUnitBuilder builder) {
        List<File> plugins = resolveNeo4jPlugins();
        Properties properties = serverFactory.getProperties(this.embedded.connectorEnabled(), this.embedded.listenAddress(), this.embedded.boltPort(),
            this.embedded.neo4jProperties(), plugins);
        builder.properties(properties);
        builder.provider(EmbeddedNeo4jXOProvider.class);
    }

    private List<File> resolveNeo4jPlugins() {
        List<Plugin> plugins = embedded.neo4jPlugins();
        if (embedded.apocEnabled()) {
            String neo4jVersion = embedded.neo4jVersion()
                .orElseThrow(() -> new IllegalStateException("Neo4j version is not configured for embedded store."));
            Plugin neo4j = PluginImpl.builder()
                .groupId("org.neo4j.procedure")
                .artifactId("apoc-core")
                .classifier(Optional.of("core"))
                .version(neo4jVersion)
                .build();
            plugins.add(neo4j);
        }
        log.info("Resolving {} Neo4j plugin(s).", plugins.size());
        return artifactProvider.resolve(plugins);
    }

    @ToString
    @Getter
    @Accessors(fluent = true)
    @Builder
    public static class PluginImpl implements Plugin {
        private final String groupId;
        private final String artifactId;
        private final String type = "jar";
        private final Optional<String> classifier;
        private final String version;
        private final List<Exclusion> exclusions = List.of();
    }

    @Override
    protected final void initialize(XOManagerFactory<?, ?, ?, ?> xoManagerFactory) {
        LOGGER.debug("Initializing embedded Neo4j server.");
        EmbeddedDatastore embeddedDatastore = xoManagerFactory.getDatastore(EmbeddedDatastore.class);
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
        // nothing to do
    }

    private EmbeddedNeo4jServerFactory getEmbeddedNeo4jServerFactory() {
        return new Neo4jCommunityServerFactory();
    }

    @Override
    protected int getDefaultAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
