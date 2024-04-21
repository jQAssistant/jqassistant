package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.plugin.ArtifactProviderFactory;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginResolverImpl;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;

import io.smallrye.config.PropertiesConfigSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliPluginResolverIT {

    @Test
    void resolve() {
        Map<String, String> configurationProperties = new HashMap<>();
        configurationProperties.put("jqassistant.plugins[0].group-id","org.jqassistant.plugin");
        configurationProperties.put("jqassistant.plugins[0].artifact-id","jqassistant-docker-plugin");
        configurationProperties.put("jqassistant.plugins[0].version","2.1.0");
        PropertiesConfigSource testConfigSource = new PropertiesConfigSource(configurationProperties, "TestConfigSource", 110);

        ConfigurationLoader<CliConfiguration> configurationLoader = new ConfigurationLoaderImpl<>(CliConfiguration.class);
        CliConfiguration cliConfiguration = configurationLoader.load(testConfigSource);

        ArtifactProviderFactory artifactProviderFactory = new ArtifactProviderFactory(new File("target/it/userhome"));
        ArtifactProvider artifactProvider = artifactProviderFactory.create(cliConfiguration);
        PluginResolver pluginResolver = new PluginResolverImpl(artifactProvider);

        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(ArtifactProviderFactory.class.getClassLoader(), cliConfiguration);

        assertThat(pluginClassLoader).isNotNull();
    }

}
