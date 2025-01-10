package com.buschmais.jqassistant.core.test.plugin;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.core.resolver.api.ArtifactProviderFactory;
import com.buschmais.jqassistant.core.resolver.configuration.ArtifactResolverConfiguration;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.runtime.api.bootstrap.PluginRepositoryFactory;
import com.buschmais.jqassistant.core.runtime.api.bootstrap.RuleProvider;
import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;

import io.smallrye.config.ConfigMapping;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.resolver.api.MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

@Slf4j
public class BootstrapIT {

    /**
     * The default directory, either relative to the current working directory or an absolute path.
     */
    public static final String DEFAULT_RULE_DIRECTORY = "jqassistant";

    @Test
    void bootstrap() throws RuleException {
        File userHome = new File(System.getProperty("user.home"));

        List<String> configProfiles = emptyList();
        ConfigSource settingsConfigSource = createMavenSettingsConfigSource(userHome, empty(), configProfiles);

        List<String> configLocations = emptyList();
        File workingDirectory = new File(".");
        ITConfiguration configuration = ConfigurationMappingLoader.builder(ITConfiguration.class, configLocations)
            .withUserHome(userHome)
            .withWorkingDirectory(workingDirectory)
            .withClasspath()
            .withEnvVariables()
            .withProfiles(configProfiles)
            .load(settingsConfigSource);

        ArtifactProvider artifactProvider = ArtifactProviderFactory.getArtifactProvider(configuration, userHome);

        PluginRepository pluginRepository = PluginRepositoryFactory.getPluginRepository(configuration, BootstrapIT.class.getClassLoader(), artifactProvider);

        RuleProvider ruleProvider = RuleProvider.create(configuration, DEFAULT_RULE_DIRECTORY, pluginRepository);

        log.info("Rule sources: {}.", ruleProvider.getRuleSources());
        log.info("Available rules: {}.", ruleProvider.getAvailableRules());
        log.info("Effective rules: {}.", ruleProvider.getEffectiveRules());
    }

    @ConfigMapping(prefix = Configuration.PREFIX)
    interface ITConfiguration extends Configuration, ArtifactResolverConfiguration {
    }

}
