package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.Optional;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class PluginInfoImpl implements PluginInfo {
    private final String id;
    private final String name;
    private final Optional<String> version;
}
