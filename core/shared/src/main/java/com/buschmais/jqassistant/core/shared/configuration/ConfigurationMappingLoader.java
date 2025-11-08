package com.buschmais.jqassistant.core.shared.configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import io.smallrye.config.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PRIVATE;

/**
 * Defines the interface for loading runtime configuration.
 * <p>
 * The mechanism is based on Eclipse Micro Profile configuration.
 */
@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class ConfigurationMappingLoader {

    /**
     * The config prefix for jQA.
     */
    public static final String PREFIX = "jqassistant";

    /**
     * The ordinal for config sources from the user home.
     */
    public static final int ORDINAL_USERHOME = 50;

    /**
     * The ordinal for config sources from classpath.
     */
    public static final int ORDINAL_CLASSPATH = 80;

    /**
     * The ordinal for config sources from the working directory.
     */
    public static final int ORDINAL_WORKING_DIRECTORY = 150;

    /**
     * The default names of configuration files
     */
    private static final List<Path> DEFAULT_CONFIG_LOCATIONS = Stream.of(".jqassistant.yml", ".jqassistant.yaml", ".jqassistant")
        .map(Paths::get)
        .collect(toUnmodifiableList());

    /**
     * Return a builder for creating a configuration mapping.
     *
     * @param configurationMapping
     *     The configuration mapping mapping.
     */
    public static <C> Builder<C> builder(Class<C> configurationMapping) {
        return new Builder<>(new ConfigurationFileLoader(), configurationMapping, emptyList());
    }

    /**
     * Return a builder for creating a configuration mapping
     *
     * @param configurationMapping
     *     The configuration mapping mapping.
     * @param configLocations
     *     The names of the configuration locations. These may either be absolute paths or relative paths to the working directory.
     */
    public static <C> Builder<C> builder(Class<C> configurationMapping, List<String> configLocations) {
        return new Builder<>(new ConfigurationFileLoader(), configurationMapping, configLocations);
    }

    /**
     * Return a builder for creating a configuration mapping
     *
     * @param configurationFileLoader
     *     The {@link ConfigurationFileLoader} to use.
     * @param configurationMapping
     *     The configuration mapping mapping.
     * @param configLocations
     *     The names of the configuration locations. These may either be absolute paths or relative paths to the working directory.
     */
    public static <C> Builder<C> builder(ConfigurationFileLoader configurationFileLoader, Class<C> configurationMapping, List<String> configLocations) {
        return new Builder<>(configurationFileLoader, configurationMapping, configLocations);
    }

    public static class Builder<C> {

        private final ConfigurationFileLoader configurationFileLoader;

        private final ConfigurationSerializer<C> configurationSerializer = new ConfigurationSerializer<>();

        private final Class<C> configurationMapping;

        private final List<Path> relativeConfigLocations;

        private final List<ConfigSource> configSources = new ArrayList<>();

        private final List<String> profiles = new ArrayList<>();

        private final Set<String> ignoreProperties = new HashSet<>();

        private Builder(ConfigurationFileLoader configurationFileLoader, Class<C> configurationMapping, List<String> configLocations) {
            this.configurationFileLoader = configurationFileLoader;
            this.configurationMapping = configurationMapping;
            if (configLocations.isEmpty()) {
                this.relativeConfigLocations = DEFAULT_CONFIG_LOCATIONS;
            } else {
                this.relativeConfigLocations = new ArrayList<>();
                for (String configLocation : configLocations) {
                    Path configLocationPath = Paths.get(configLocation);
                    if (configLocationPath.isAbsolute()) {
                        this.configSources.addAll(configurationFileLoader.getYamlConfigSources(configLocationPath, ORDINAL_WORKING_DIRECTORY));
                    } else {
                        this.relativeConfigLocations.add(configLocationPath);
                    }
                }
            }
        }

        /**
         * Add YAML configs from the user home directory.
         *
         * @param userHome
         *     The user home.
         * @return The {@link Builder}.
         */
        public Builder<C> withUserHome(File userHome) {
            configSources.addAll(configurationFileLoader.getYamlConfigSources(userHome, DEFAULT_CONFIG_LOCATIONS, ORDINAL_USERHOME));
            return this;
        }

        /**
         * Add YAML configs from the working workingDirectory.
         *
         * @param workingDirectory
         *     The working workingDirectory.
         * @return The {@link Builder}.
         */
        public Builder<C> withWorkingDirectory(File workingDirectory) {
            return withDirectory(workingDirectory, ORDINAL_WORKING_DIRECTORY);
        }

        /**
         * Add YAML configs from the given directory.
         *
         * @param directory
         *     The directory.
         * @param ordinal
         *     The ordinal to apply.
         * @return The {@link Builder}.
         */
        public Builder<C> withDirectory(File directory, int ordinal) {
            configSources.addAll(configurationFileLoader.getYamlConfigSources(directory, relativeConfigLocations, ordinal));
            return this;
        }

        /**
         * Add YAML configs from the classpath.
         *
         * @return The {@link Builder}.
         */
        public Builder<C> withClasspath() {
            this.configSources.addAll(configurationFileLoader.loadYamlConfigResources(ORDINAL_CLASSPATH));
            return this;
        }

        /**
         * Add YAML configs from the environment variables.
         *
         * @return The {@link Builder}.
         */
        public Builder<C> withEnvVariables() {
            this.configSources.add(new EnvConfigSource() {
            });
            return this;
        }

        /**
         * Add profiles to activate.
         *
         * @param profiles
         *     The profiles.
         * @return The {@link Builder}.
         */
        public Builder<C> withProfiles(List<String> profiles) {
            this.profiles.addAll(profiles);
            return this;
        }

        /**
         * Add properties to ignore.
         *
         * @param ignoreProperties
         *     The properties to ignore.
         * @return The {@link Builder}.
         */
        public Builder<C> withIgnoreProperties(Collection<String> ignoreProperties) {
            this.ignoreProperties.addAll(ignoreProperties);
            return this;
        }

        /**
         * Load the configuration using the given directory including
         * <p/>
         * - yml/yaml files present in the given configuration directory
         * - system properties
         * - environment variables
         *
         * @param additionalConfigSources
         *     Additional {@link ConfigSource}s to consider, e.g. from a CLI or Maven Mojo.
         * @return The configuration.
         */
        public C load(ConfigSource... additionalConfigSources) {
            log.debug("Loading configuration using profiles {}. ", profiles);
            // Create intermediate configuration with applied profiles and interpolated properties (without validation)
            SmallRyeConfig config = new SmallRyeConfigBuilder().withSources(this.configSources)
                .withSources(additionalConfigSources)
                .withProfiles(this.profiles)
                .withInterceptors(new ExpressionConfigSourceInterceptor())
                .withMapping(configurationMapping)
                .withValidateUnknown(false)
                .build();
            if (log.isDebugEnabled()) {
                logConfigProblems(config);
            }
            C configMapping = config.getConfigMapping(configurationMapping);
            if (log.isDebugEnabled()) {
                log.debug("Loaded configuration from {} config sources:\n{}", additionalConfigSources.length, configurationSerializer.toYaml(configMapping));
            }
            return configMapping;
        }

        private void logConfigProblems(SmallRyeConfig interpolatedConfig) {
            Map<String, String> filteredProperties = stream(interpolatedConfig.getPropertyNames()
                .spliterator(), false).filter(property -> property.startsWith(PREFIX))
                .filter(property -> !ignoreProperties.contains(property))
                .filter(property -> interpolatedConfig.getRawValue(property) != null)
                .collect(toMap(property -> property, interpolatedConfig::getRawValue, (s1, s2) -> null, TreeMap::new));
            log.debug("jQAssistant config properties:");
            for (Map.Entry<String, String> entry : filteredProperties.entrySet()) {
                log.debug("\t{}={}", entry.getKey(), entry.getValue());
            }
            try {
                new SmallRyeConfigBuilder().withMapping(configurationMapping)
                    .withSources(new PropertiesConfigSource(filteredProperties, "jQAssistant Configuration", ConfigSource.DEFAULT_ORDINAL))
                    .build();
            } catch (ConfigValidationException configValidationException) {
                for (int i = 0; i < configValidationException.getProblemCount(); i++) {
                    log.debug(configValidationException.getProblem(i)
                        .getMessage());
                }
            }
        }
    }
}
