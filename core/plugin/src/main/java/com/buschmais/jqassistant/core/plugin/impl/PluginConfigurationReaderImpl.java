package com.buschmais.jqassistant.core.plugin.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.rule.impl.reader.XmlHelper;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin reader implementation.
 */
public class PluginConfigurationReaderImpl implements PluginConfigurationReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationReaderImpl.class);

    private static final Schema SCHEMA = XmlHelper.getSchema(PLUGIN_SCHEMA_RESOURCE);

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
     * @param pluginClassLoader The class loader to use for detecting plugins.
     */
    public PluginConfigurationReaderImpl(ClassLoader pluginClassLoader) {
        this.pluginClassLoader = pluginClassLoader;
        Map<String, String> namespaceMappings = new HashMap<>();
        namespaceMappings.put("http://www.buschmais.com/jqassistant/core/plugin/schema/v1.0", "http://www.buschmais.com/jqassistant/core/plugin/schema/v1.2");
        namespaceMappings.put("http://www.buschmais.com/jqassistant/core/plugin/schema/v1.1", "http://www.buschmais.com/jqassistant/core/plugin/schema/v1.2");
        this.jaxbUnmarshaller = new JAXBUnmarshaller<>(JqassistantPlugin.class, SCHEMA, namespaceMappings);
    }

    @Override
    public ClassLoader getClassLoader() {
        return pluginClassLoader;
    }

    /**
     * Read the catalogs from an {@link URL}.
     *
     * @param pluginUrl The {@link URL}.
     * @return The {@link JqassistantPlugin}.
     */
    private JqassistantPlugin readPlugin(URL pluginUrl) {
        try (InputStream inputStream = new BufferedInputStream(pluginUrl.openStream())) {
            return jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read plugin from " + pluginUrl.toString(), e);
        }
    }

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from
     * the current classpath.
     *
     * @return The plugins which can be resolved from the current classpath.
     */
    @Override
    public List<JqassistantPlugin> getPlugins() {
        if (this.plugins == null) {
            final Enumeration<URL> resources;
            try {
                resources = pluginClassLoader.getResources(PLUGIN_RESOURCE);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot get plugin resources.", e);
            }
            this.plugins = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                LOGGER.debug("Reading plugin descriptor from '{}'.", url);
                this.plugins.add(readPlugin(url));
            }
            SortedSet<String> pluginNames = new TreeSet<>();
            for (JqassistantPlugin plugin : plugins) {
                pluginNames.add(plugin.getName());
            }
            LOGGER.info("Loaded jQAssistant plugins {}.", pluginNames);
        }
        return this.plugins;
    }
}
