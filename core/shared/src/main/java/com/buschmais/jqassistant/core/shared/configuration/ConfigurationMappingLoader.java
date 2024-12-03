package com.buschmais.jqassistant.core.shared.configuration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

import io.smallrye.config.*;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.stream.Collectors.*;
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

    private static final String YAML = ".yaml";
    private static final String YML = ".yml";

    /**
     * The default names of configuration files
     */
    private static final List<Path> DEFAULT_CONFIG_LOCATIONS = Stream.of(".jqassistant.yml", ".jqassistant.yaml", ".jqassistant")
        .map(Paths::get)
        .collect(toUnmodifiableList());

    private static final String CLASSPATH_RESOURCE = ".jqassistant.yml";

    /**
     * Return a builder for creating a configuration mapping.
     *
     * @param configurationMapping
     *     The configuration mapping mapping.
     */
    public static <C> Builder<C> builder(Class<C> configurationMapping) {
        return new Builder<>(configurationMapping, emptyList());
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
        return new Builder<>(configurationMapping, configLocations);
    }

    public static class Builder<C> {

        private final ConfigurationSerializer<C> configurationSerializer = new ConfigurationSerializer<>();

        private final Class<C> configurationMapping;

        private final List<Path> relativeConfigLocations;

        private final List<ConfigSource> configSources = new ArrayList<>();

        private final List<String> profiles = new ArrayList<>();

        private final Set<String> ignoreProperties = new HashSet<>();

        private Builder(Class<C> configurationMapping, List<String> configLocations) {
            this.configurationMapping = configurationMapping;
            if (configLocations.isEmpty()) {
                this.relativeConfigLocations = DEFAULT_CONFIG_LOCATIONS;
            } else {
                this.relativeConfigLocations = new ArrayList<>();
                for (String configLocation : configLocations) {
                    Path configLocationPath = Paths.get(configLocation);
                    if (configLocationPath.isAbsolute()) {
                        this.configSources.addAll(getExternalYamlConfigSources(configLocationPath, ORDINAL_WORKING_DIRECTORY));
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
            configSources.addAll(getExternalYamlConfigSources(userHome, DEFAULT_CONFIG_LOCATIONS, ORDINAL_USERHOME));
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
            configSources.addAll(getExternalYamlConfigSources(directory, relativeConfigLocations, ordinal));
            return this;
        }

        /**
         * Add YAML configs from the classpath.
         *
         * @return The {@link Builder}.
         */
        public Builder<C> withClasspath() {
            this.configSources.addAll(getYamlConfigSourceFromClasspath());
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

        private List<ConfigSource> getExternalYamlConfigSources(File directory, List<Path> configLocations, int ordinal) {
            List<ConfigSource> yamlConfigSources = new ArrayList<>();
            for (Path configLocation : configLocations) {
                Path path = directory.toPath()
                    .resolve(configLocation);
                yamlConfigSources.addAll(getExternalYamlConfigSources(path, ordinal));
            }
            return yamlConfigSources;
        }

        private List<ConfigSource> getExternalYamlConfigSources(Path configLocationPath, int ordinal) {
            File file = configLocationPath.toFile();
            if (!file.exists()) {
                return emptyList();
            }
            if (file.isDirectory()) {
                log.info("Scanning for configuration files in directory '{}'.", configLocationPath.toAbsolutePath());
                List<Path> configurationFiles = findYamlConfigurationFiles(configLocationPath);
                return configurationFiles.stream()
                    .map(path -> getYamlConfigSource(path, ordinal))
                    .collect(toList());
            } else {
                return List.of(getYamlConfigSource(configLocationPath, ordinal));
            }
        }

        private List<Path> findYamlConfigurationFiles(Path configurationDirectory) {
            List<Path> configurationFiles = new ArrayList<>();
            try {
                walkFileTree(configurationDirectory, new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String fileName = file.toFile()
                            .getName();
                        if (fileName.endsWith(YAML) || fileName.endsWith(YML)) {
                            configurationFiles.add(file);
                        }
                        return CONTINUE;
                    }

                });
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot list files in configuration directory " + configurationDirectory, e);
            }
            return configurationFiles;
        }

        private ConfigSource getYamlConfigSource(Path path, int ordinal) {
            try {
                return getYamlConfigSource(path.toUri()
                    .toURL(), ordinal);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot convert path '" + path + "' ot URL.");
            }
        }

        private List<ConfigSource> getYamlConfigSourceFromClasspath() {
            try {
                Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(CLASSPATH_RESOURCE);
                return list(resources).stream()
                    .map(resource -> getYamlConfigSource(resource, ORDINAL_CLASSPATH))
                    .collect(toUnmodifiableList());
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot get classpath resources for " + CLASSPATH_RESOURCE, e);
            }
        }

        private ConfigSource getYamlConfigSource(URL url, int ordinal) {
            log.info("Loading YAML configuration from '{}' (priority: {}).", url, ordinal);
            try {
                return new YamlConfigSource(url, ordinal);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot create YAML config source from URL " + url, e);
            }
        }
    }
}
