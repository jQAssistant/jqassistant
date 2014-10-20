package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.ScannerType;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;

/**
 * Scanner plugin repository implementation.
 */
public class ScannerPluginRepositoryImpl extends AbstractPluginRepository implements ScannerPluginRepository {

    private final List<ScannerPlugin<?, ?>> scannerPlugins;

    /**
     * Constructor.
     */
    public ScannerPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader, Map<String, Object> properties) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        List<JqassistantPlugin> plugins = pluginConfigurationReader.getPlugins();
        this.scannerPlugins = getScannerPlugins(plugins, properties);
    }

    @Override
    public List<ScannerPlugin<?, ?>> getScannerPlugins() throws PluginRepositoryException {
        return scannerPlugins;
    }

    private <T extends ScannerPlugin> List<T> getScannerPlugins(List<JqassistantPlugin> plugins, Map<String, Object> properties)
            throws PluginRepositoryException {
        List<T> scannerPlugins = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ScannerType scannerType = plugin.getScanner();
            if (scannerType != null) {
                for (String scannerPluginName : scannerType.getClazz()) {
                    T scannerPlugin = createInstance(scannerPluginName);
                    if (scannerPlugin != null) {
                        // properties is mutable, so every plugin should get its
                        // own copy
                        scannerPlugin.initialize(new HashMap<>(properties));
                        scannerPlugins.add(scannerPlugin);
                    }
                }
            }
        }
        return scannerPlugins;
    }
}
