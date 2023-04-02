package com.buschmais.jqassistant.core.runtime.impl.plugin;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo;

import org.jqassistant.schema.plugin.v1.JqassistantPlugin;

class PluginInfoImpl implements PluginInfo {
    private String id;
    private String name;

    PluginInfoImpl(String pluginId, String pluginName) {
        id = pluginId;
        name = pluginName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    public static PluginInfo of(JqassistantPlugin plugin) {
        String id = plugin.getId();
        String name = plugin.getName();

        return new PluginInfoImpl(id, name);
    }
}
