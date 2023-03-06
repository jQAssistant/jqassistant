package com.buschmais.jqassistant.core.plugin.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginClassLoader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.PluginResolver;
import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import static java.util.stream.Collectors.toList;
import static org.eclipse.aether.util.artifact.JavaScopes.RUNTIME;

/**
 * Implementation of a {@link PluginResolver} based on Eclipse Aether.
 */
@Slf4j
@RequiredArgsConstructor
public class AetherPluginResolverImpl implements PluginResolver {

    private final RepositorySystem repositorySystem;

    private final RepositorySystemSession repositorySystemSession;

    private final List<RemoteRepository> repositories;

    @Override
    public PluginClassLoader createClassLoader(ClassLoader parent, List<Plugin> plugins) {
        if (!plugins.isEmpty()) {
            log.info("Resolving {} plugins and required dependencies.", plugins.size());
            List<Dependency> requiredPlugins = getRequiredPluginDependencies(plugins);
            DependencyResult dependencyResult = resolvePlugins(requiredPlugins);
            return new PluginClassLoader(parent, getDependencyURLs(dependencyResult));
        }
        return new PluginClassLoader(parent);
    }

    private DependencyResult resolvePlugins(List<Dependency> dependencies) {
        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(RUNTIME);
        DependencyResult dependencyResult = resolveDependencies(classpathFilter, dependencies);
        if (log.isDebugEnabled()) {
            logDependencyTree(dependencyResult.getRoot(), 0);
        }
        return dependencyResult;
    }

    private List<Dependency> getRequiredPluginDependencies(List<Plugin> plugins) {
        return plugins.stream().flatMap(plugin -> createPluginDependencies(plugin).stream()).collect(toList());
    }

    private List<Dependency> createPluginDependencies(Plugin plugin) {
        return plugin.artifactId().stream().map(
            artifactId -> new Dependency(new DefaultArtifact(plugin.groupId(), artifactId, plugin.classifier().orElse(null), plugin.type(), plugin.version()),
                RUNTIME)).collect(toList());
    }

    private DependencyResult resolveDependencies(DependencyFilter classpathFilter, List<Dependency> dependencies) {
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setDependencies(dependencies);
        collectRequest.setRepositories(repositories);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);
        try {
            return repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        } catch (DependencyResolutionException e) {
            throw new PluginRepositoryException("Cannot resolve plugin dependencies", e);
        }
    }

    private void logDependencyTree(DependencyNode node, int indent) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            builder.append(' ');
        }
        Artifact artifact = node.getArtifact();
        if (artifact != null) {
            log.info("{}{}", builder, artifact);
        }
        for (DependencyNode child : node.getChildren()) {
            logDependencyTree(child, indent + 2);
        }
    }

    private List<URL> getDependencyURLs(DependencyResult dependencyResult) {
        return dependencyResult.getArtifactResults().stream().map(artifactResult -> {
            try {
                return artifactResult.getArtifact().getFile().toURI().toURL();
            } catch (MalformedURLException e) {
                throw new PluginRepositoryException("Cannot convert artifact " + artifactResult.getArtifact() + " to URL", e);
            }
        }).collect(toList());
    }
}
