package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.shared.xml.JAXBHelper;
import com.buschmais.jqassistant.core.shared.xml.XmlHelper;

import org.jqassistant.schema.plugin.v2.JqassistantPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin reader implementation.
 */
public class PluginConfigurationReaderImpl implements PluginConfigurationReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationReaderImpl.class);

    private static final Schema SCHEMA = XmlHelper.getSchema(PLUGIN_SCHEMA_RESOURCE);

    private final ClassLoader pluginClassLoader;

    private final JAXBHelper<JqassistantPlugin> jaxbHelper;

    /**
     * Map of pLugin descriptors identified by the plugin base URL
     */
    private Map<URL, JqassistantPlugin> plugins = null;

    /**
     * Constructor.
     *
     * @param pluginClassLoader
     *     The class loader to use for detecting plugins.
     */
    public PluginConfigurationReaderImpl(PluginClassLoader pluginClassLoader) {
        this.pluginClassLoader = pluginClassLoader;
        this.jaxbHelper = new JAXBHelper<>(JqassistantPlugin.class, SCHEMA, NAMESPACE);
    }

    @Override
    public ClassLoader getClassLoader() {
        return pluginClassLoader;
    }

    /**
     * Read the catalogs from an {@link URL}.
     *
     * @param pluginUrl
     *     The {@link URL}.
     * @return The {@link JqassistantPlugin}.
     */
    protected JqassistantPlugin readPlugin(URL pluginUrl) {
        try {
            return jaxbHelper.unmarshal(pluginUrl);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read plugin from " + pluginUrl, e);
        }
    }

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from the
     * current classpath.
     *
     * @return The plugins which can be resolved from the current classpath.
     */
    @Override
    public Map<URL, JqassistantPlugin> getPlugins() {
        if (this.plugins == null) {
            LOGGER.info("Scanning for jQAssistant plugins...");

            PluginIdGenerator idGenerator = new PluginIdGenerator();
            TreeSet<String> ids = new TreeSet<>();
            Enumeration<URL> resources = getPluginClassLoaderResources();
            this.plugins = new HashMap<>();

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                URL pluginBaseURL = getPluginBaseURL(url);

                LOGGER.debug("Reading plugin descriptor from '{}'.", url);
                JqassistantPlugin plugin = idGenerator.apply(readPlugin(url));

                if (ids.add(plugin.getId())) {
                    plugins.put(pluginBaseURL, plugin);
                } else {
                    JqassistantPlugin loadedPlugin = plugins.values()
                        .stream()
                        .filter(p -> p.getId()
                            .equals(plugin.getId()))
                        .findFirst()
                        .get();
                    LOGGER.warn("Skipping plugin '{}' with id '{}' as it uses the same id as the already loaded plugin '{}'.", plugin.getName(), plugin.getId(),
                        loadedPlugin.getName());
                }
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

    private static URL getPluginBaseURL(URL url) {
        String value = url.toString();
        try {
            return new URL(value.substring(0, value.lastIndexOf(PluginConfigurationReader.PLUGIN_RESOURCE)));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot create plugin base URL from " + value, e);
        }
    }
}
