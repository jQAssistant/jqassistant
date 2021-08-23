package com.buschmais.jqassistant.core.plugin.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlHelper;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;

import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * Plugin reader implementation.
 */
public class PluginConfigurationReaderImpl implements PluginConfigurationReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationReaderImpl.class);

    private static final Schema SCHEMA = XmlHelper.getSchema(PLUGIN_SCHEMA_RESOURCE);
    private static final String NAMESPACE = "http://schema.jqassistant.org/plugin/v1.10";

    private final ClassLoader pluginClassLoader;

    private JAXBUnmarshaller<JqassistantPlugin> jaxbUnmarshaller;

    private List<JqassistantPlugin> plugins = null;

    /**
     * Default constructor.
     */
    public PluginConfigurationReaderImpl() {
        this(PluginConfigurationReader.class.getClassLoader());
    }

    /**
     * Constructor.
     *
     * @param pluginClassLoader
     *            The class loader to use for detecting plugins.
     */
    public PluginConfigurationReaderImpl(ClassLoader pluginClassLoader) {
        this.pluginClassLoader = pluginClassLoader;
        this.jaxbUnmarshaller = new JAXBUnmarshaller<>(JqassistantPlugin.class, SCHEMA, NAMESPACE);
    }

    @Override
    public ClassLoader getClassLoader() {
        return pluginClassLoader;
    }

    /**
     * Read the catalogs from an {@link URL}.
     *
     * @param pluginUrl
     *            The {@link URL}.
     * @return The {@link JqassistantPlugin}.
     */
    protected JqassistantPlugin readPlugin(URL pluginUrl) {
        try (InputStream inputStream = new BufferedInputStream(pluginUrl.openStream())) {
            return jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read plugin from " + pluginUrl.toString(), e);
        }
    }

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from the
     * current classpath.
     *
     * @return The plugins which can be resolved from the current classpath.
     */
    @Override
    public List<JqassistantPlugin> getPlugins() {
        if (this.plugins == null) {
            LOGGER.info("Scanning for jQAssistant plugins...");

            PluginIdGenerator idGenerator = new PluginIdGenerator();
            TreeSet<String> ids = new TreeSet<>();
            Enumeration<URL> resources = getPluginClassLoaderResources();
            this.plugins = new ArrayList<>();

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                LOGGER.debug("Reading plugin descriptor from '{}'.", url);
                JqassistantPlugin plugin = idGenerator.apply(readPlugin(url));

                if (ids.contains(plugin.getId())) {
                    JqassistantPlugin loadedPlugin = plugins.stream().filter(p -> p.getId().equals(plugin.getId()))
                                                            .findFirst().get();
                    String message = format("Unable to load plugin '%s' with id '%s', as the same id is used by " +
                                     "plugin '%s'", plugin.getName(), plugin.getId(), loadedPlugin.getName());

                    LOGGER.error(message);
                    throw new PluginRepositoryException(message);
                }

                LOGGER.info("Loaded plugin '{}' with id '{}'", plugin.getName(),
                            plugin.getId());
                ids.add(plugin.getId());
                plugins.add(plugin);
            }
        }

        return plugins;
    }

    protected Enumeration<URL> getPluginClassLoaderResources() {
        try {
            return pluginClassLoader.getResources(PLUGIN_RESOURCE);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get plugin resources.", e);
        }

    }
}
