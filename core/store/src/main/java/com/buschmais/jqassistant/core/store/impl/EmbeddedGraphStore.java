package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
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
        cleanApocPlugins();
        configureApoc();
        List<File> plugins = resolveNeo4jPlugins();
        Properties properties = serverFactory.getProperties(this.embedded.connectorEnabled(), this.embedded.listenAddress(), this.embedded.boltPort(),
            this.embedded.neo4jProperties(), plugins);
        builder.properties(properties);
        builder.provider(EmbeddedNeo4jXOProvider.class);
    }

    /**
     * Convert the store URI to a File, handling both relative (opaque) and absolute (hierarchical) file URIs.
     *
     * @return The store directory as a File, or null if the URI is not a file URI.
     */
    private File getStoreDirectory() {
        String scheme = uri.getScheme();
        if (!"file".equalsIgnoreCase(scheme)) {
            return null;
        }
        // Handle both relative (opaque) and absolute (hierarchical) file URIs
        // Relative: file:target/store (opaque, getPath() returns null)
        // Absolute: file:///path/to/store (hierarchical, getPath() returns the path)
        if (uri.isOpaque()) {
            return new File(uri.getSchemeSpecificPart());
        } else {
            return new File(uri);
        }
    }

    /**
     * Clean existing APOC jars from the plugins directory to prevent version conflicts.
     */
    private void cleanApocPlugins() {
        if (!embedded.apocEnabled()) {
            return;
        }
        File storeDirectory = getStoreDirectory();
        if (storeDirectory == null) {
            return;
        }
        File pluginsDirectory = new File(storeDirectory, "plugins");
        if (pluginsDirectory.exists()) {
            File[] apocJars = pluginsDirectory.listFiles((dir, name) -> name.startsWith("apoc-") && name.endsWith(".jar"));
            if (apocJars != null && apocJars.length > 0) {
                for (File apocJar : apocJars) {
                    if (apocJar.delete()) {
                        log.info("Removed existing APOC jar '{}'.", apocJar.getName());
                    } else {
                        log.warn("Could not remove existing APOC jar '{}'.", apocJar.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Configure APOC by creating a conf directory and writing apoc.conf if APOC properties are specified.
     */
    private void configureApoc() {
        File storeDirectory = getStoreDirectory();
        if (storeDirectory == null) {
            // Only configure APOC for file-based stores
            return;
        }
        File confDirectory = new File(storeDirectory, "conf");
        if (!confDirectory.exists() && !confDirectory.mkdirs()) {
            log.warn("Could not create Neo4j conf directory: {}", confDirectory.getAbsolutePath());
            return;
        }
        // Set NEO4J_CONF system property so APOC can find apoc.conf
        System.setProperty("NEO4J_CONF", confDirectory.getAbsolutePath());
        log.info("Set NEO4J_CONF to '{}'.", confDirectory.getAbsolutePath());

        Map<String, String> apocProperties = embedded.apocProperties();
        if (!apocProperties.isEmpty()) {
            File apocConfFile = new File(confDirectory, "apoc.conf");
            try (FileWriter writer = new FileWriter(apocConfFile)) {
                for (Map.Entry<String, String> entry : apocProperties.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
                }
                log.info("Created APOC configuration file '{}' with {} properties.", apocConfFile.getAbsolutePath(), apocProperties.size());
            } catch (IOException e) {
                log.warn("Could not write APOC configuration file: {}", apocConfFile.getAbsolutePath(), e);
            }
        }
    }

    private List<File> resolveNeo4jPlugins() {
        List<Plugin> plugins = embedded.neo4jPlugins();
        if (embedded.apocEnabled()) {
            String neo4jVersion = embedded.neo4jVersion()
                    .orElseThrow(() -> new IllegalStateException("Neo4j version is not configured for embedded store."));
            Plugin apocCore = PluginImpl.builder()
                    .groupId("org.neo4j.procedure")
                    .artifactId("apoc-core")
                    .version(neo4jVersion)
                    .build();
            Plugin apocCommon = PluginImpl.builder()
                    .groupId("org.neo4j.procedure")
                    .artifactId("apoc-common")
                    .version(neo4jVersion)
                    .build();
            plugins.add(apocCore);
            plugins.add(apocCommon);
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
        @Builder.Default
        private final Optional<String> classifier = Optional.empty();
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
