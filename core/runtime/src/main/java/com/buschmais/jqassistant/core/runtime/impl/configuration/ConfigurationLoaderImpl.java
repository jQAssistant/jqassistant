package com.buschmais.jqassistant.core.runtime.impl.configuration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;

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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Implementation of the {@link ConfigurationLoader}.
 */
@Slf4j
public class ConfigurationLoaderImpl implements ConfigurationLoader {

    public static final String YAML = ".yaml";
    public static final String YML = ".yml";

    public static final String CLASSPATH_RESOURCE = ".jqassistant.yml";

    private final List<ConfigSource> yamlConfigSources;

    /**
     * Constructor using a configuration directory for looking up YAML config files.
     *
     * @param workingDirectory
     *     The working directory for loading YAML config files.
     * @param configLocations
     *     The name of the configuration directory relative to the working directory.
     */
    public ConfigurationLoaderImpl(File workingDirectory, Optional<List<String>> configLocations) {
        List<ConfigSource> configSources = new ArrayList<>();
        configSources.addAll(getExternalYamlConfigSources(workingDirectory, configLocations));
        configSources.addAll(getYamlConfigSourceFromClasspath());
        this.yamlConfigSources = configSources;
    }

    /**
     * Constructor.
     */
    public ConfigurationLoaderImpl() {
        this.yamlConfigSources = emptyList();
    }

    @Override
    public <C extends Configuration> C load(Class<C> configurationMapping, ConfigSource... configSources) {
        SmallRyeConfig config = new SmallRyeConfigBuilder().withMapping(configurationMapping)
            .withSources(yamlConfigSources)
            .withSources(new EnvConfigSource() {
            })
            .withSources(configSources)
            .withValidateUnknown(false)
            .withInterceptors(new ExpressionConfigSourceInterceptor())
            .build();
        return config.getConfigMapping(configurationMapping);
    }

    private List<ConfigSource> getExternalYamlConfigSources(File workingDirectory, Optional<List<String>> optionalConfigLocations) {
        List<ConfigSource> configSources = new ArrayList<>();
        for (String configLocations : optionalConfigLocations.orElse(DEFAULT_CONFIG_LOCATIONS)) {
            File file = workingDirectory.toPath()
                .resolve(Paths.get(configLocations))
                .toFile();
            if (file.isDirectory()) {
                configSources.addAll(getYamlConfigSources(file));
            } else if (file.exists()) {
                configSources.add(getYamlConfigSource(file.toPath()));
            }
        }
        return configSources;
    }

    private List<ConfigSource> getYamlConfigSources(File configurationDirectory) {
        if (configurationDirectory.exists()) {
            log.info("Loading configuration from directory '{}'.", configurationDirectory.getAbsolutePath());
            List<Path> configurationFiles = getYamlConfigurationFiles(configurationDirectory);
            return configurationFiles.stream()
                .map(path -> {
                    return getYamlConfigSource(path);
                })
                .collect(toList());
        }
        log.info("Configuration directory '{}' does not exist, skipping.", configurationDirectory);
        return emptyList();
    }

    private List<Path> getYamlConfigurationFiles(File configurationDirectory) {
        List<Path> configurationFiles = new ArrayList<>();
        try {
            walkFileTree(configurationDirectory.toPath(), new SimpleFileVisitor<Path>() {

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

    private YamlConfigSource getYamlConfigSource(Path path) {
        try {
            return getYamlConfigSource(path.toUri()
                .toURL());
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
                .map(resource -> getYamlConfigSource(resource))
                .collect(toUnmodifiableList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot get classpath resources for " + CLASSPATH_RESOURCE, e);
        }
    }

    private YamlConfigSource getYamlConfigSource(URL url) {
        return getYamlConfigSource(url, YamlConfigSource.DEFAULT_ORDINAL);
    }

    private YamlConfigSource getYamlConfigSource(URL url, int ordinal) {
        try {
            return new YamlConfigSource(url, ordinal);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create YAML config source from URL " + url, e);
        }
    }

}
