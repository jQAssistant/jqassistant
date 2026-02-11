package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.buschmais.jqassistant.core.report.api.BuildConfigBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationFileLoader;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.source.EmptyConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.MavenProjectConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.MavenPropertiesConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.SettingsConfigSource;
import com.buschmais.jqassistant.scm.maven.provider.ConfigurationFileLoaderProvider;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.RequiredArgsConstructor;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@RequiredArgsConstructor
final class MavenConfigurationFactory {

    public static final String PROPERTY_CONFIGURATION_LOCATIONS = "jqassistant.configuration.locations";

    private static final int CONFIGURATION_ORDINAL_EXECUTION_ROOT = 100;

    private final ConfigurationFileLoaderProvider configurationFileLoaderProvider;

    /**
     * Retrieve the runtime configuration.
     * <p>
     * The configuration directory is assumed to be located within the execution root of the Maven session.
     *
     * @return The {@link MavenConfiguration}.
     */
    MavenConfiguration getConfiguration(MavenSession session, MavenProject currentProject, List<String> configurationLocations, String yaml,
        Properties properties, boolean isConnectorRequired) {
        ConfigSource buildConfigSource = BuildConfigBuilder.getConfigSource(session.getTopLevelProject()
            .getName(), session.getStartTime()
            .toInstant()
            .atZone(ZoneId.systemDefault()));
        ConfigurationBuilder mojoConfigurationBuilder = new ConfigurationBuilder("MojoConfigSource", 110);
        // activate connector (depending on goal)
        if (isConnectorRequired) {
            mojoConfigurationBuilder.with(Embedded.class, Embedded.CONNECTOR_ENABLED, true);
        }
        ConfigSource mojoConfigSource = mojoConfigurationBuilder.build();

        MavenProjectConfigSource projectConfigSource = new MavenProjectConfigSource(currentProject);
        SettingsConfigSource settingsConfigSource = new SettingsConfigSource(session.getSettings());
        MavenPropertiesConfigSource projectPropertiesConfigSource = new MavenPropertiesConfigSource(currentProject.getProperties(), "Maven Project Properties");
        MavenPropertiesConfigSource userPropertiesConfigSource = new MavenPropertiesConfigSource(session.getUserProperties(), "Maven Session User Properties ");
        MavenPropertiesConfigSource systemPropertiesConfigSource = new MavenPropertiesConfigSource(session.getSystemProperties(),
            "Maven Session System Properties");
        ConfigSource yamlConfiguration = getYamlPluginConfiguration(yaml);
        ConfigSource propertiesConfiguration = getPropertiesPluginConfiguration(properties);

        ConfigSource[] configSources = new ConfigSource[] { buildConfigSource, mojoConfigSource, projectConfigSource, settingsConfigSource,
            projectPropertiesConfigSource, userPropertiesConfigSource, systemPropertiesConfigSource, yamlConfiguration, propertiesConfiguration };
        File userHome = new File(System.getProperty("user.home"));
        File executionRootDirectory = new File(session.getExecutionRootDirectory());
        List<String> activatedProfiles = new ArrayList<>(session.getProjectBuildingRequest()
            .getActiveProfileIds());
        currentProject.getActiveProfiles()
            .stream()
            .map(Profile::getId)
            .forEach(activatedProfiles::add);
        ConfigurationFileLoader configurationFileLoader = configurationFileLoaderProvider.getConfigurationFileLoader();
        ConfigurationMappingLoader.Builder<MavenConfiguration> builder = ConfigurationMappingLoader.builder(configurationFileLoader, MavenConfiguration.class,
                configurationLocations)
            .withUserHome(userHome)
            .withDirectory(executionRootDirectory, CONFIGURATION_ORDINAL_EXECUTION_ROOT)
            .withEnvVariables()
            .withClasspath()
            .withProfiles(activatedProfiles)
            .withIgnoreProperties(Set.of(PROPERTY_CONFIGURATION_LOCATIONS));
        if (!executionRootDirectory.equals(currentProject.getBasedir())) {
            builder.withWorkingDirectory(currentProject.getBasedir());
        }
        return builder.load(configSources);
    }

    private ConfigSource getYamlPluginConfiguration(String yaml) {
        return isNotEmpty(yaml) ?
            new YamlConfigSource("Maven plugin execution YAML configuration", yaml, MavenPropertiesConfigSource.CONFIGURATION_ORDINAL_MAVEN_PROPERTIES) :
            EmptyConfigSource.INSTANCE;
    }

    private ConfigSource getPropertiesPluginConfiguration(Properties properties) {
        return properties != null ?
            new PropertiesConfigSource(properties, "Maven plugin execution properties configuration",
                MavenPropertiesConfigSource.CONFIGURATION_ORDINAL_MAVEN_PROPERTIES) :
            EmptyConfigSource.INSTANCE;
    }

}
