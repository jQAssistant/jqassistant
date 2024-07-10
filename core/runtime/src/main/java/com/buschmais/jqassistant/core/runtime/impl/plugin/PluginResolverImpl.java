package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepositoryException;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.Plugin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of a {@link PluginResolver} based on Eclipse Aether.
 */
@Slf4j
@RequiredArgsConstructor
public class PluginResolverImpl implements PluginResolver {

    private final ArtifactProvider artifactProvider;

    @Override
    public PluginClassLoader createClassLoader(ClassLoader parent, Configuration configuration) {
        List<Plugin> plugins = new ArrayList<>();
        plugins.addAll(configuration.defaultPlugins());
        plugins.addAll(configuration.plugins());
        if (!plugins.isEmpty()) {
            log.info("Resolving {} plugins and required dependencies.", plugins.size());
            List<File> files = artifactProvider.resolve(plugins);
            return new PluginClassLoader(parent, getDependencyURLs(files));
        }
        return new PluginClassLoader(parent);
    }

    private List<URL> getDependencyURLs(List<File> files) {
        return files.stream()
            .map(file -> {
            try {
                return file.toURI()
                    .toURL();
            } catch (MalformedURLException e) {
                throw new PluginRepositoryException("Cannot convert file " + file + " to URL", e);
            }
        }).collect(toList());
    }
}
