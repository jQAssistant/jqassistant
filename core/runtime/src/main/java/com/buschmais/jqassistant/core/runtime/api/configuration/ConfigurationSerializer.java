package com.buschmais.jqassistant.core.runtime.api.configuration;

public interface ConfigurationSerializer<C> {

    String toYaml(C configuration);
}
