package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.embedded.InstrumentationProvider;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.configuration.GraphDatabaseInternalSettings;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.io.ByteUnit;

import static java.lang.Boolean.FALSE;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class Neo4jCommunityServerFactory implements EmbeddedNeo4jServerFactory {

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jCommunityNeoServer();
    }

    @Override
    public Properties getProperties(boolean connectorEnabled, String listenAddress, Integer boltPort, Optional<File> pluginDirectory) {
        EmbeddedNeo4jXOProvider.PropertiesBuilder propertiesBuilder = EmbeddedNeo4jXOProvider.propertiesBuilder()
            .property(GraphDatabaseSettings.procedure_unrestricted, List.of("*"))
            // keep disk footprint small (TX logs)
            .property(GraphDatabaseSettings.keep_logical_logs, FALSE.toString())
            .property(GraphDatabaseSettings.logical_log_rotation_threshold, ByteUnit.mebiBytes(25L))
            // deactivate unnecessary logging
            .property(GraphDatabaseSettings.log_queries, GraphDatabaseSettings.LogQueryLevel.OFF)
            .property(GraphDatabaseInternalSettings.dump_diagnostics, false)
            // don't wait on server shutdown
            .property(GraphDatabaseInternalSettings.netty_server_shutdown_quiet_period, 0);
        pluginDirectory.ifPresent(dir -> {
            prepareClassloader(dir.toPath());
            propertiesBuilder.property(GraphDatabaseSettings.plugin_dir, dir.toPath());
        });
        if (connectorEnabled) {
            propertiesBuilder.property(BoltConnector.enabled, true);
            propertiesBuilder.property(BoltConnector.listen_address, new SocketAddress(listenAddress, boltPort));
        }
        Properties properties = propertiesBuilder.build();
        // set string properties which are not available for Neo4j v4
        // deactivate internal debug logs
        properties.setProperty("neo4j.server.logs.debug.enabled", FALSE.toString());
        // deactivate user data collector
        properties.setProperty("neo4j.dbms.usage_report.enabled", FALSE.toString());
        return properties;
    }

    /**
     * Scans the given plugin directory for JAR files and adds them to the {@link ClassLoader} used by Neo4j.
     *
     * @param pluginDir
     *     The plugin directory.
     */
    private static void prepareClassloader(Path pluginDir) {
        Set<Path> paths;
        try (Stream<Path> pathStream = Files.find(pluginDir, 1, (p, a) -> p.getFileName()
            .toString()
            .endsWith(".jar"))) {
            paths = pathStream.collect(toSet());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list plugin directory " + pluginDir, e);
        }
        getClasspathAppender().accept(paths);

    }

    /**
     * Provides a {@link Consumer} to add the {@link Path} of a JAR file to the {@link ClassLoader} used by Neo4j.
     */
    private static Consumer<Set<Path>> getClasspathAppender() {
        ClassLoader neo4jClassLoader = GraphDatabaseSettings.class.getClassLoader();
        log.info("Using Neo4j classloader {}", neo4jClassLoader);
        if (neo4jClassLoader instanceof URLClassLoader) {
            return getURLClassLoaderAppender((URLClassLoader) neo4jClassLoader);
        } else {
            return getInstrumentationAppender();
        }
    }

    /**
     * Uses an existing {@link URLClassLoader} (e.g. Maven) by making the method addURL accessible.
     */
    private static Consumer<Set<Path>> getURLClassLoaderAppender(URLClassLoader classLoader) {
        Method method;
        try {
            method = classLoader.getClass()
                .getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot use URLClassLoader to extend classpath.", e);
        }
        method.setAccessible(true);
        Set<URL> existingUrls = Set.of(classLoader.getURLs());
        return paths -> {
            for (Path path : paths) {
                URL url;
                try {
                    url = path.toUri()
                        .toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
                if (!existingUrls.contains(url)) {
                    try {
                        method.invoke(classLoader, url);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Cannot add URL to classloader.", e);
                    }
                }
            }
        };
    }

    /**
     * Uses {@link java.lang.instrument.Instrumentation} if available (e.g. CLI).
     */
    private static Consumer<Set<Path>> getInstrumentationAppender() {
        return paths -> InstrumentationProvider.INSTANCE.getInstrumentation()
            .ifPresentOrElse(instrumentation -> {
                for (Path path : paths) {
                    JarFile jarFile;
                    try {
                        jarFile = new JarFile(path.toFile());
                    } catch (IOException e) {
                        throw new IllegalStateException("Cannot create JAR from file " + path.toAbsolutePath(), e);
                    }
                    instrumentation.appendToSystemClassLoaderSearch(jarFile);

                }
            }, () -> log.warn("Runtime instrumentation is not available, Neo4j plugins might not work."));
    }
}
