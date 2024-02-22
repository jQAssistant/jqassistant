package com.buschmais.jqassistant.neo4j.embedded.neo4jv4;

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
import java.util.function.Consumer;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

import org.neo4j.configuration.GraphDatabaseInternalSettings;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.io.ByteUnit;

import static java.lang.Boolean.FALSE;

public class Neo4jV4ServerFactory implements EmbeddedNeo4jServerFactory {

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV4CommunityNeoServer();
    }

    @Override
    public Properties getProperties(boolean connectorEnabled, String listenAddress, Integer boltPort, Optional<File> pluginDirectory) {
        EmbeddedNeo4jXOProvider.PropertiesBuilder propertiesBuilder = EmbeddedNeo4jXOProvider.propertiesBuilder()
            .property(GraphDatabaseSettings.keep_logical_logs, FALSE.toString())
            .property(GraphDatabaseSettings.logical_log_rotation_threshold, ByteUnit.mebiBytes(25L))
            .property(GraphDatabaseSettings.procedure_unrestricted, List.of("*"))
            .property(GraphDatabaseInternalSettings.dump_diagnostics, false);
        pluginDirectory.ifPresent(dir -> {
            prepareClassloader(dir.toPath());
            propertiesBuilder.property(GraphDatabaseSettings.plugin_dir, dir.toPath());
        });
        if (connectorEnabled) {
            propertiesBuilder.property(BoltConnector.enabled, true);
            propertiesBuilder.property(BoltConnector.listen_address, new SocketAddress(listenAddress, boltPort));
        }
        return propertiesBuilder.build();
    }

    private static void prepareClassloader(Path pluginDir) {
        Consumer<Path> consumer = getClasspathAppender();
        try {
            Files.find(pluginDir, 1, (p, a) -> p.getFileName()
                    .toString()
                    .endsWith(".jar"))
                .forEach(file -> consumer.accept(file));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list plugin directory " + pluginDir, e);
        }
    }

    private static Consumer<Path> getClasspathAppender() {
        ClassLoader neo4jClassLoader = GraphDatabaseSettings.class.getClassLoader();
        try {
            if (neo4jClassLoader instanceof URLClassLoader) {
                return getURLClassLoaderAppender(neo4jClassLoader);
            } else {
                return getAppClassLoaderAppender(neo4jClassLoader);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Consumer<Path> getURLClassLoaderAppender(ClassLoader classLoader) throws NoSuchMethodException {
        Method method = classLoader.getClass()
            .getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        return path -> {
            try {
                method.invoke(classLoader, path.toUri()
                    .toURL());
            } catch (ReflectiveOperationException | MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private static Consumer<Path> getAppClassLoaderAppender(ClassLoader classLoader) throws NoSuchMethodException {
        Method method = classLoader.getClass()
            .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
        method.setAccessible(true);
        return path -> {
            try {
                method.invoke(classLoader, path.toAbsolutePath()
                    .toString());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
