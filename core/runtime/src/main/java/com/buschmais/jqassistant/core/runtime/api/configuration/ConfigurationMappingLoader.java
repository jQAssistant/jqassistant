package com.buschmais.jqassistant.core.runtime.api.configuration;

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

import io.smallrye.config.EnvConfigSource;
import io.smallrye.config.ExpressionConfigSourceInterceptor;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Defines the interface for loading runtime configuration.
 * <p>
 * The mechanism is based on Eclipse Micro Profile configuration.
 */
@Slf4j
public class ConfigurationMappingLoader {

    /**
     * The default names of configuration files
     */
    public static final List<Path> DEFAULT_CONFIG_LOCATIONS = List.of(".jqassistant.yml", ".jqassistant.yaml", ".jqassistant")
        .stream()
        .map(Paths::get)
        .collect(toList());

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

    private static final String CLASSPATH_RESOURCE = ".jqassistant.yml";

    /**
     * Return a builder for creating a {@link Configuration}.
     *
     * @param configurationMapping
     *     The {@link Configuration} mapping.
     */
    public static <C extends Configuration> Builder<C> builder(Class<C> configurationMapping) {
        return new Builder<>(configurationMapping, emptyList());
    }

    /**
     * Return a builder for creating a {@link Configuration}.
     *
     * @param configurationMapping
     *     The {@link Configuration} mapping.
     * @param configLocations
     *     The names of the configuration locations. These may either be absolute paths or relative paths to the working directory.
     */
    public static <C extends Configuration> Builder<C> builder(Class<C> configurationMapping, List<String> configLocations) {
        return new Builder<>(configurationMapping, configLocations);
    }

    public static class Builder<C extends Configuration> {

        private final ConfigurationSerializer<C> configurationSerializer = new ConfigurationSerializer<>();

        private final Class<C> configurationMapping;

        private final List<Path> configLocations = new ArrayList<>();

        private final Set<URL> yamlConfigFiles = new HashSet<>();

        private final List<ConfigSource> configSources = new ArrayList<>();

        private final List<String> profiles = new ArrayList<>();

        private Builder(Class<C> configurationMapping, List<String> configLocations) {
            this.configurationMapping = configurationMapping;
            for (String configLocation : configLocations) {
                Path configLocationPath = Paths.get(configLocation);
                if (configLocationPath.isAbsolute()) {
                    configSources.addAll(getExternalYamlConfigSources(configLocationPath, ORDINAL_WORKING_DIRECTORY));
                } else {
                    this.configLocations.add(configLocationPath);
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
            configSources.addAll(getExternalYamlConfigSources(directory, configLocations.isEmpty() ? DEFAULT_CONFIG_LOCATIONS : configLocations, ordinal));
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
         * Load the {@link Configuration} using the given directory including
         * <p/>
         * - yml/yaml files present in the given configuration directory
         * - system properties
         * - environment variables
         *
         * @param additionalConfigSources
         *     Additional {@link ConfigSource}s to consider, e.g. from a CLI or Maven Mojo.
         * @return The {@link Configuration}.
         */
        public C load(ConfigSource... additionalConfigSources) {
            SmallRyeConfig config = new SmallRyeConfigBuilder().withMapping(configurationMapping)
                .withSources(this.configSources)
                .withSources(additionalConfigSources)
                .withValidateUnknown(false)
                .withInterceptors(new ExpressionConfigSourceInterceptor())
                .withProfiles(this.profiles)
                .build();
            C configMapping = config.getConfigMapping(configurationMapping);
            if (log.isDebugEnabled()) {
                log.debug("Loaded configuration from {} config sources:\n{}", additionalConfigSources.length, configurationSerializer.toYaml(configMapping));
            }
            return configMapping;
        }

        private List<ConfigSource> getExternalYamlConfigSources(File directory, List<Path> configLocations, int ordinal) {
            List<ConfigSource> configSources = new ArrayList<>();
            for (Path configLocation : configLocations) {
                Path path = directory.toPath()
                    .resolve(configLocation);
                configSources.addAll(getExternalYamlConfigSources(path, ordinal));
            }
            return configSources;
        }

        private List<ConfigSource> getExternalYamlConfigSources(Path configLocationPath, int ordinal) {
            File file = configLocationPath.toFile();
            if (!file.exists()) {
                return emptyList();
            }
            if (file.isDirectory()) {
                log.info("Loading configuration from directory '{}'.", configLocationPath.toAbsolutePath());
                List<Path> configurationFiles = findYamlConfigurationFiles(configLocationPath);
                return configurationFiles.stream()
                    .map(path -> getYamlConfigSource(path, ordinal))
                    .flatMap(Optional::stream)
                    .collect(toList());
            } else {
                return getYamlConfigSource(configLocationPath, ordinal).map(List::of)
                    .orElse(emptyList());
            }
        }

        private List<Path> findYamlConfigurationFiles(Path configurationDirectory) {
            List<Path> configurationFiles = new ArrayList<>();
            try {
                walkFileTree(configurationDirectory, new SimpleFileVisitor<Path>() {

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

        private Optional<ConfigSource> getYamlConfigSource(Path path, int ordinal) {
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
                    .flatMap(Optional::stream)
                    .collect(toUnmodifiableList());
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot get classpath resources for " + CLASSPATH_RESOURCE, e);
            }
        }

        private Optional<ConfigSource> getYamlConfigSource(URL url, int ordinal) {
            if (yamlConfigFiles.add(url)) {
                log.info("Loading configuration from '{}' (priority: {}).", url, ordinal);
                try {
                    return Optional.of(new YamlConfigSource(url, ordinal));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Cannot create YAML config source from URL " + url, e);
                }
            }
            return empty();
        }

    }
}
