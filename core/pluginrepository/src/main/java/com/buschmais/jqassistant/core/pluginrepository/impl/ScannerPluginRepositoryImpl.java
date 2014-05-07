package com.buschmais.jqassistant.core.pluginrepository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ScannerType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.StoreType;
import com.buschmais.jqassistant.core.pluginrepository.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Scanner plugin repository implementation.
 */
public class ScannerPluginRepositoryImpl extends PluginRepositoryImpl implements ScannerPluginRepository {

    private List<Class<?>> descriptorTypes;
    private List<ProjectScannerPlugin> projectScannerPlugins;
    private List<FileScannerPlugin> fileScannerPlugins;

    /**
     * Constructor.
     */
    public ScannerPluginRepositoryImpl(Store store, Map<String, Object> properties) throws PluginRepositoryException {
        List<JqassistantPlugin> plugins = getPlugins();
        this.descriptorTypes = getDescriptorTypes(plugins);
        this.projectScannerPlugins = getScannerPlugins(plugins, ProjectScannerPlugin.class, store, properties);
        this.fileScannerPlugins = getScannerPlugins(plugins, FileScannerPlugin.class, store, properties);
    }

    @Override
    public List<Class<?>> getDescriptorTypes() throws PluginRepositoryException {
        return descriptorTypes;
    }

    @Override
    public List<FileScannerPlugin> getFileScannerPlugins() throws PluginRepositoryException {
        return fileScannerPlugins;
    }

    @Override
    public List<ProjectScannerPlugin> getProjectScannerPlugins() throws PluginRepositoryException {
        return projectScannerPlugins;
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

    private <T extends ScannerPlugin> List<T> getScannerPlugins(List<JqassistantPlugin> plugins, Class<T> pluginClass, Store store,
            Map<String, Object> properties) throws PluginRepositoryException {
        List<T> scannerPlugins = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ScannerType scannerType = plugin.getScanner();
            if (scannerType != null) {
                for (String scannerPluginName : scannerType.getPlugin()) {
                    // if one plugin fails, continue with other plugins
                    // catch throwable because of NoClassDefFoundError
                    try {
                        T scannerPlugin = createInstance(pluginClass, scannerPluginName);
                        if (scannerPlugin != null) {
                            scannerPlugin.initialize(store, new HashMap<>(properties));
                            // properties is mutable, so every plugin should get
                            // its own copy
                            scannerPlugins.add(scannerPlugin);
                        }
                    } catch (Throwable e) {
                        System.err.println(String.format("Could not create plugin %s of class %s because of exception %s", scannerPluginName, pluginClass,
                                e.toString())); // FIXME use logger here
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
     * @param clazz
     *            The expected class to be cast to.
     * @param typeName
     *            The type name.
     * @param <T>
     *            The type.
     * @return The scanner plugin instance.
     * @throws com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException
     */
    private <T> T createInstance(Class<? extends ScannerPlugin> clazz, String typeName) throws PluginRepositoryException {
        Class<T> type = getType(typeName);
        try {
            T newInstance = type.newInstance();
            return clazz.isInstance(newInstance) ? newInstance : null;
        } catch (InstantiationException e) {
            throw new PluginRepositoryException("Cannot create instance of class " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new PluginRepositoryException("Cannot access class " + typeName, e);
        }
    }
}
