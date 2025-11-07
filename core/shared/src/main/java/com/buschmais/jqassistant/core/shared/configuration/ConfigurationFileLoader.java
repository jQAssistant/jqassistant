package com.buschmais.jqassistant.core.shared.configuration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
public class ConfigurationFileLoader {

    @Builder
    @EqualsAndHashCode
    @ToString
    private static class ConfigLocationKey {

        private final Path path;

        private final int ordinal;

    }

    private static final String CLASSPATH_RESOURCE = ".jqassistant.yml";

    private static final String YAML = ".yaml";
    private static final String YML = ".yml";

    private final Cache<ConfigLocationKey, List<ConfigSource>> configSourceCache = Caffeine.newBuilder()
        .build();

    List<ConfigSource> getYamlConfigSources(File directory, List<Path> configLocations, int ordinal) {
        List<ConfigSource> yamlConfigSources = new ArrayList<>();
        for (Path configLocation : configLocations) {
            Path path = directory.toPath()
                .resolve(configLocation);
            yamlConfigSources.addAll(getYamlConfigSources(path, ordinal));
        }
        return yamlConfigSources;
    }

    List<ConfigSource> getYamlConfigSources(Path configLocationPath, int ordinal) {
        return configSourceCache.get(ConfigLocationKey.builder()
            .path(configLocationPath)
            .ordinal(ordinal)
            .build(), configLocationKey -> loadYamlConfigSources(configLocationPath, ordinal));
    }

    List<ConfigSource> loadYamlConfigResources(int ordinal) {
        try {
            Enumeration<URL> resources = Thread.currentThread()
                .getContextClassLoader()
                .getResources(CLASSPATH_RESOURCE);
            return list(resources).stream()
                .map(resource -> getYamlConfigSource(resource, ordinal))
                .collect(toUnmodifiableList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot get classpath resources for " + CLASSPATH_RESOURCE, e);
        }
    }

    private List<ConfigSource> loadYamlConfigSources(Path configLocationPath, int ordinal) {
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

    private ConfigSource getYamlConfigSource(URL url, int ordinal) {
        log.info("Loading YAML configuration from '{}' (priority: {}).", url, ordinal);
        try {
            return new YamlConfigSource(url, ordinal);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create YAML config source from URL " + url, e);
        }
    }
}
