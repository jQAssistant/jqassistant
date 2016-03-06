package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;

/**
 * Abstract base implementation of a plugin repository.
 */
public abstract class AbstractPluginRepository {

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
        this.classLoader = pluginConfigurationReader.getClassLoader();
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
            return (Class<T>) classLoader.loadClass(typeName);
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
        }
    }

}
