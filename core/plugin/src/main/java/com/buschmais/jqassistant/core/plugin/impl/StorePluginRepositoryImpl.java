package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.schema.v1.ClassListType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

/**
 * Scanner plugin repository implementation.
 */
public class StorePluginRepositoryImpl extends AbstractPluginRepository implements StorePluginRepository {

    private final List<Class<?>> descriptorTypes;

    private final List<Class<?>> procedureTypes;

    private final List<Class<?>> functionTypes;

    /**
     * Constructor.
     */
    public StorePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
        this.descriptorTypes = getTypes(plugins,plugin -> plugin.getModel());
        this.procedureTypes = getTypes(plugins, plugin -> plugin.getProcedure());
        this.functionTypes = getTypes(plugins, plugin -> plugin.getFunction());
    }

    @Override
    public List<Class<?>> getDescriptorTypes() {
        return descriptorTypes;
    }


    @Override
    public List<Class<?>> getProcedureTypes() {
        return procedureTypes;
    }

    @Override
    public List<Class<?>> getFunctionTypes() {
        return functionTypes;
    }

    private List<Class<?>> getTypes(List<JqassistantPlugin> plugins, Function<JqassistantPlugin, ClassListType> classListSupplier) {
        List<Class<?>> types = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            ClassListType type = classListSupplier.apply(plugin);
            if (type != null) {
                for (String typeName : type.getClazz()) {
                    types.add(getType(typeName));
                }
            }
        }
        return types;
    }

    private List<Class<?>> getDescriptorTypes(List<JqassistantPlugin> plugins) {
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
