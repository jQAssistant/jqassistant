package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.jqassistant.schema.plugin.v2.ClassListType;
import org.jqassistant.schema.plugin.v2.JqassistantPlugin;

/**
 * Scanner plugin repository implementation.
 */
public class StorePluginRepositoryImpl extends AbstractPluginRepository implements StorePluginRepository {

    private final List<Class<?>> descriptorTypes;

    /**
     * Constructor.
     */
    public StorePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
        this.descriptorTypes = getTypes(plugins, JqassistantPlugin::getModel);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public List<Class<?>> getDescriptorTypes() {
        return descriptorTypes;
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

}
