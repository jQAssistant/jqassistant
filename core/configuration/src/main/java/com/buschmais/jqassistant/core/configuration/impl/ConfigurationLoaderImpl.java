package com.buschmais.jqassistant.core.configuration.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the {@link ConfigurationLoader}.
 */
@Slf4j
public class ConfigurationLoaderImpl implements ConfigurationLoader {

    public static final String YAML = ".yaml";
    public static final String YML = ".yml";

    private List<ConfigSource> yamlConfigSources;

    /**
     * Constructor using a configuration directory for looking up YAML config files.
     *
     * @param workingDirectory
     *     The working directory for loading YAML config files.
     * @param configurationDirectory
     *     The name of the configuration directory relative to the working directory.
     */
    public ConfigurationLoaderImpl(File workingDirectory, String configurationDirectory) {
        this.yamlConfigSources = getYamlConfigSources(workingDirectory, configurationDirectory);
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
            .addDefaultSources()
            .withSources(yamlConfigSources)
            .withSources(configSources)
            .withValidateUnknown(false)
            .build();
        return config.getConfigMapping(configurationMapping);
    }

    private List<ConfigSource> getYamlConfigSources(File workingDirectory, String configurationDirectory) {
        List<ConfigSource> configSources = new ArrayList<>(getYamlConfigSources(new File(workingDirectory, configurationDirectory)));
        for (String configurationFile : DEFAULT_CONFIGURATION_FILES) {
            getYamlConfigFile(workingDirectory, configurationFile).ifPresent(configSource -> configSources.add(configSource));
        }
        return configSources;
    }

    private Optional<ConfigSource> getYamlConfigFile(File workingDirectory, String fileName) {
        File configFile = new File(workingDirectory, fileName);
        if (configFile.exists()) {
            return of(getYamlConfigSource(configFile.toPath()));
        }
        return empty();
    }

    private List<ConfigSource> getYamlConfigSources(File configurationDirectory) {
        if (configurationDirectory.exists()) {
            log.info("Loading configuration from directory '{}'.", configurationDirectory.getAbsolutePath());
            List<Path> configurationFiles = getConfigurationFiles(configurationDirectory);
            return getYamlConfigSources(configurationFiles);
        }
        log.info("Configuration directory '{}' does not exist, skipping.", configurationDirectory);
        return emptyList();
    }

    private List<Path> getConfigurationFiles(File configurationDirectory) {
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
            log.warn("Cannot list files in configuration directory " + configurationDirectory, e);
        }
        return configurationFiles;
    }

    private List<ConfigSource> getYamlConfigSources(List<Path> configurationFiles) {
        return configurationFiles.stream()
            .map(path -> {
                return getYamlConfigSource(path);
            })
            .collect(toList());
    }

    private YamlConfigSource getYamlConfigSource(Path path) {
        try {
            URL url = path.toUri()
                .toURL();
            return new YamlConfigSource(url);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
