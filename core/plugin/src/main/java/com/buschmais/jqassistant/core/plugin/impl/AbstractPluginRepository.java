package com.buschmais.jqassistant.core.plugin.impl;

import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;

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
     *            The
     *            {@link com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader}
     *            .
     */
    protected AbstractPluginRepository(PluginConfigurationReader pluginConfigurationReader) {
        this.plugins = pluginConfigurationReader.getPlugins();
        this.classLoader = pluginConfigurationReader.getClassLoader();
        LOGGER.debug("Using classloader '{}'", this.classLoader);
    }

    /**
     * Create and return an instance of the given type name.
     *
     * @param typeName
     *            The type name.
     * @param <T>
     *            The type.
     * @return The instance.
     * @throws com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException
     *             If the instance cannot be created.
     */
    protected <T> Class<T> getType(String typeName) throws PluginRepositoryException {
        try {
            return (Class<T>) classLoader.loadClass(typeName.trim());
        } catch (ClassNotFoundException e) {
            throw new PluginRepositoryException("Cannot find class " + typeName, e);
        }
    }

    /**
     * Create an instance of the given scanner plugin class.
     *
     * @param typeName
     *            The type name.
     * @param <T>
     *            The type.
     * @return The scanner plugin instance.
     * @throws com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException
     */
    protected <T> T createInstance(String typeName) throws PluginRepositoryException {
        Class<T> type = getType(typeName.trim());
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new PluginRepositoryException("Cannot create instance of class " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new PluginRepositoryException("Cannot access class " + typeName, e);
        } catch (LinkageError e) {
            throw new PluginRepositoryException("Cannot load plugin class " + typeName, e);
        }
    }

}
