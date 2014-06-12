package com.buschmais.jqassistant.core.pluginrepository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ScannerType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.StoreType;
import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepository;
import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.pluginrepository.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Scanner plugin repository implementation.
 */
public class ScannerPluginRepositoryImpl implements ScannerPluginRepository {

    private final List<Class<?>> descriptorTypes;
    private final List<ScannerPlugin<?>> scannerPlugins;

    /**
     * Constructor.
     */
    public ScannerPluginRepositoryImpl(PluginRepository pluginRepository, Store store, Map<String, Object> properties) throws PluginRepositoryException {
        List<JqassistantPlugin> plugins = pluginRepository.getPlugins();
        this.descriptorTypes = getDescriptorTypes(plugins);
        this.scannerPlugins = getScannerPlugins(plugins, store, properties);
    }

    @Override
    public List<Class<?>> getDescriptorTypes() throws PluginRepositoryException {
        return descriptorTypes;
    }

    @Override
    public List<ScannerPlugin<?>> getScannerPlugins() throws PluginRepositoryException {
        return scannerPlugins;
    }

    private List<Class<?>> getDescriptorTypes(List<JqassistantPlugin> plugins) throws PluginRepositoryException {
        List<Class<?>> types = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            StoreType storeType = plugin.getStore();
            if (storeType != null) {
                for (String typeName : storeType.getType()) {
                    types.add(getType(typeName));
                }
            }
        }
        return types;
    }

    private <T extends ScannerPlugin> List<T> getScannerPlugins(List<JqassistantPlugin> plugins, Store store, Map<String, Object> properties)
            throws PluginRepositoryException {
        List<T> scannerPlugins = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ScannerType scannerType = plugin.getScanner();
            if (scannerType != null) {
                for (String scannerPluginName : scannerType.getPlugin()) {
                    T scannerPlugin = createInstance(scannerPluginName);
                    if (scannerPlugin != null) {
                        scannerPlugin.initialize(store, new HashMap<>(properties));
                        // properties is mutable, so every plugin should get its
                        // own copy
                        scannerPlugins.add(scannerPlugin);
                    }
                }
            }
        }
        return scannerPlugins;
    }

    /**
     * Create and return an instance of the given type name.
     * 
     * @param typeName
     *            The type name.
     * @param <T>
     *            The type.
     * @return The instance.
     * @throws com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException
     *             If the instance cannot be created.
     */
    private <T> Class<T> getType(String typeName) throws PluginRepositoryException {
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
     * @throws com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException
     */
    private <T> T createInstance(String typeName) throws PluginRepositoryException {
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
