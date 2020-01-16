package com.buschmais.jqassistant.core.plugin.impl;

import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;

import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of a plugin repository.
 */
public abstract class AbstractPluginRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginRepository.class);

    protected final List<JqassistantPlugin> plugins;

    /*
     * The class loader to use for loading classes and resources.
     */
    private final ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     *            The {@link PluginConfigurationReader} .
     */
    protected AbstractPluginRepository(PluginConfigurationReader pluginConfigurationReader) {
        this.plugins = pluginConfigurationReader.getPlugins();
        this.classLoader = pluginConfigurationReader.getClassLoader();
        LOGGER.debug("Using classloader '{}'", this.classLoader);
    }

    /**
     * Get the class for the given type name.
     *
     * @param typeName
     *            The type name.
     * @param <T>
     *            The type name.
     * @return The class.
     */
    protected <T> Class<T> getType(String typeName) {
        try {
            return (Class<T>) classLoader.loadClass(typeName.trim());
        } catch (ClassNotFoundException | LinkageError e) {
            // Catching class loader related errors for logging a message which plugin class
            // actually caused the problem.
            throw new PluginRepositoryException("Cannot find or load class " + typeName, e);
        }
    }

    /**
     * Create an instance of the given scanner plugin class.
     *
     * @param typeName
     *            The type name.
     * @param <T>
     *            The type.
     * @return The plugin instance.
     */
    protected <T> T createInstance(String typeName) {
        Class<T> type = getType(typeName.trim());
        try {
            return type.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new PluginRepositoryException("Cannot load or create instance of class " + type.getName(), e);
        }
    }

}
