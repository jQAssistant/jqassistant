package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;

public abstract class AbstractPluginRepository {

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
            return (Class<T>) Class.forName(typeName);
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
        Class<T> type = getType(typeName);
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new PluginRepositoryException("Cannot create instance of class " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new PluginRepositoryException("Cannot access class " + typeName, e);
        }
    }

}
