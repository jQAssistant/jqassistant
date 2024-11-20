package com.buschmais.jqassistant.core.runtime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.resolver.api.ArtifactProviderFactory;
import com.buschmais.jqassistant.core.resolver.configuration.ArtifactResolverConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginResolverImpl;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.PropertiesConfigSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PluginResolverIT {

    @ConfigMapping(prefix = Configuration.PREFIX)
    interface TestConfiguration extends Configuration, ArtifactResolverConfiguration {
    }

    @Test
    void resolve() {
        Map<String, String> configurationProperties = new HashMap<>();
        configurationProperties.put("jqassistant.plugins[0].group-id", "org.jqassistant.plugin");
        configurationProperties.put("jqassistant.plugins[0].artifact-id", "jqassistant-docker-plugin");
        configurationProperties.put("jqassistant.plugins[0].version", "2.1.0");
        PropertiesConfigSource testConfigSource = new PropertiesConfigSource(configurationProperties, "TestConfigSource", 110);

        TestConfiguration configuration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .load(testConfigSource);

        ArtifactProviderFactory artifactProviderFactory = new ArtifactProviderFactory(new File("target/it/userhome"));
        ArtifactProvider artifactProvider = artifactProviderFactory.create(configuration);
        PluginResolver pluginResolver = new PluginResolverImpl(artifactProvider);

        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(ArtifactProviderFactory.class.getClassLoader(), configuration);

        assertThat(pluginClassLoader).isNotNull();
    }

}
