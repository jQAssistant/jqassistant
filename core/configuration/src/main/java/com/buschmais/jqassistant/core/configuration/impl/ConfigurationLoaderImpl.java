package com.buschmais.jqassistant.core.configuration.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the {@link ConfigurationLoader}.
 */
@Slf4j
public class ConfigurationLoaderImpl implements ConfigurationLoader {

    @Override
    public Configuration load(File configurationDirectory, ConfigSource... configSources) {
        List<ConfigSource> yamlConfigSources = getYamlConfigSources(configurationDirectory);
        SmallRyeConfig config = new SmallRyeConfigBuilder().withMapping(Configuration.class).addDefaultSources().withSources(yamlConfigSources)
            .withSources(configSources).withValidateUnknown(false).build();
        return config.getConfigMapping(Configuration.class);
    }

    private List<ConfigSource> getYamlConfigSources(File configurationDirectory) {
        if (configurationDirectory.exists()) {
            log.info("Loading configuration from directory '{}'.", configurationDirectory);
            List<Path> configurationFiles = getConfigurationFiles(configurationDirectory);
            return getYamlConfigSources(configurationFiles);
        }
        log.info("Configuration directory '{}' does not exist, skipping.", configurationDirectory);
        return emptyList();
    }

    private List<Path> getConfigurationFiles(File configurationDirectory) {
        try (Stream<Path> list = Files.list(configurationDirectory.toPath())) {
            return list.collect(toList());
        } catch (IOException e) {
            log.warn("Cannot list files in configuration directory " + configurationDirectory, e);
        }
        return null;
    }

    private List<ConfigSource> getYamlConfigSources(List<Path> configurationFiles) {
        return configurationFiles.stream().map(path -> {
            try {
                URL url = path.toUri().toURL();
                return new YamlConfigSource(url);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(toList());
    }

}
