package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.rule.impl.reader.XmlHelper;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;

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

    private final JAXBUnmarshaller<JqassistantPlugin> jaxbUnmarshaller;

    private List<JqassistantPlugin> plugins = null;

    /**
     * Constructor.
     *
     * @param pluginClassLoader
     *     The class loader to use for detecting plugins.
     */
    public PluginConfigurationReaderImpl(PluginClassLoader pluginClassLoader) {
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
     *     The {@link URL}.
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

                if (ids.add(plugin.getId())) {
                    plugins.add(plugin);
                } else {
                    JqassistantPlugin loadedPlugin = plugins.stream()
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
}
