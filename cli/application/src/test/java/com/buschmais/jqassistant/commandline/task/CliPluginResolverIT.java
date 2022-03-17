package com.buschmais.jqassistant.commandline.task;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.plugin.PluginResolverFactory;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.configuration.impl.ConfigurationLoaderImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginClassLoader;
import com.buschmais.jqassistant.core.plugin.api.PluginResolver;

import io.smallrye.config.PropertiesConfigSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliPluginResolverIT {

    @Test
    void resolve() {
        Map<String, String> configurationProperties = new HashMap<>();
        configurationProperties.put("jqassistant.plugins[0].group-id","org.jqassistant.contrib.plugin");
        configurationProperties.put("jqassistant.plugins[0].artifact-id","jqassistant-docker-plugin");
        configurationProperties.put("jqassistant.plugins[0].version","1.11.0");
        PropertiesConfigSource testConfigSource = new PropertiesConfigSource(configurationProperties, "TestConfigSource", 110);

        ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();
        CliConfiguration cliConfiguration = configurationLoader.load(CliConfiguration.class, testConfigSource);

        PluginResolverFactory pluginResolverFactory = new PluginResolverFactory();
        PluginResolver pluginResolver = pluginResolverFactory.create(cliConfiguration);

        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(PluginResolverFactory.class.getClassLoader(), cliConfiguration.plugins());

        assertThat(pluginClassLoader).isNotNull();
    }

}
