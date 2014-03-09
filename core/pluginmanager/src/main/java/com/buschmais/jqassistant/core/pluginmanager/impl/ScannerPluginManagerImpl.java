package com.buschmais.jqassistant.core.pluginmanager.impl;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ScannerType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.StoreType;
import com.buschmais.jqassistant.core.pluginmanager.api.ScannerPluginManager;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Scanner plugin repository implementation.
 */
public class ScannerPluginManagerImpl extends PluginManagerImpl implements ScannerPluginManager {

    private List<Class<?>> descriptorTypes;
    private List<ProjectScannerPlugin> projectScannerPlugins;
    private List<FileScannerPlugin> fileScannerPlugins;

    /**
     * Constructor.
     */
    public ScannerPluginManagerImpl(Store store, Properties properties) throws PluginReaderException {
        List<JqassistantPlugin> plugins = getPlugins();
        this.descriptorTypes = getDescriptorTypes(plugins);
        this.projectScannerPlugins = getScannerPlugins(plugins, ProjectScannerPlugin.class, store, properties);
        this.fileScannerPlugins = getScannerPlugins(plugins, FileScannerPlugin.class, store, properties);
    }

    @Override
    public List<Class<?>> getDescriptorTypes() throws PluginReaderException {
        return descriptorTypes;
    }

    @Override
    public List<FileScannerPlugin> getFileScannerPlugins() throws PluginReaderException {
        return fileScannerPlugins;
    }

    @Override
    public List<ProjectScannerPlugin> getProjectScannerPlugins() throws PluginReaderException {
        return projectScannerPlugins;
    }

    private List<Class<?>> getDescriptorTypes(List<JqassistantPlugin> plugins) throws PluginReaderException {
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

    private <T extends ScannerPlugin> List<T> getScannerPlugins(List<JqassistantPlugin> plugins, Class<T> pluginClass, Store store, Properties properties) throws PluginReaderException {
        List<T> scannerPlugins = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ScannerType scannerType = plugin.getScanner();
            if (scannerType != null) {
                for (String scannerPluginName : scannerType.getPlugin()) {
                    T scannerPlugin = createInstance(pluginClass, scannerPluginName);
                    if (scannerPlugin != null) {
                        scannerPlugin.initialize(store, properties);
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
     * @param typeName The type name.
     * @param <T>      The type.
     * @return The instance.
     * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException If the instance cannot be created.
     */
    private <T> Class<T> getType(String typeName) throws PluginReaderException {
        try {
            return (Class<T>) Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new PluginReaderException("Cannot find class " + typeName, e);
        }
    }

    /**
     * Create an instance of the given scanner plugin class.
     *
     * @param clazz    The expected class to be cast to.
     * @param typeName The type name.
     * @param <T>      The type.
     * @return The scanner plugin instance.
     * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException
     */
    private <T> T createInstance(Class<? extends ScannerPlugin> clazz, String typeName) throws PluginReaderException {
        Class<T> type = getType(typeName);
        try {
            T newInstance = type.newInstance();
            return clazz.isInstance(newInstance) ? newInstance : null;
        } catch (InstantiationException e) {
            throw new PluginReaderException("Cannot create instance of class " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new PluginReaderException("Cannot access class " + typeName, e);
        }
    }
}
