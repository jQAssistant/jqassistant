package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.schema.v1.ClassListType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;

/**
 * Scanner plugin repository implementation.
 */
public class ModelPluginRepositoryImpl extends AbstractPluginRepository implements ModelPluginRepository {

    private final List<Class<?>> descriptorTypes;

    /**
     * Constructor.
     */
    public ModelPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        this.descriptorTypes = getDescriptorTypes(plugins);
    }

    @Override
    public List<Class<?>> getDescriptorTypes() {
        return descriptorTypes;
    }

    private List<Class<?>> getDescriptorTypes(List<JqassistantPlugin> plugins) throws PluginRepositoryException {
        List<Class<?>> types = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ClassListType modelTypes = plugin.getModel();
            if (modelTypes != null) {
                for (String typeName : modelTypes.getClazz()) {
                    types.add(getType(typeName));
                }
            }
        }
        return types;
    }
}
