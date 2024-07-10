package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepositoryException;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.Plugin;

import lombok.Getter;
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
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import static java.util.stream.Collectors.toList;
import static org.eclipse.aether.util.artifact.JavaScopes.RUNTIME;

@Slf4j
@Getter
@RequiredArgsConstructor
public class AetherArtifactProvider implements ArtifactProvider {

    private final RepositorySystem repositorySystem;

    private final RepositorySystemSession repositorySystemSession;

    private final List<RemoteRepository> repositories;

    public List<File> resolve(List<Plugin> plugins) {
        List<Dependency> requiredPlugins = getDependencies(plugins);
        DependencyResult dependencyResult = resolvePlugins(requiredPlugins);
        return asFiles(dependencyResult);
    }

    private List<Dependency> getDependencies(List<Plugin> plugins) {
        return plugins.stream()
            .flatMap(plugin -> getPluginDependencies(plugin).stream())
            .collect(toList());
    }

    private DependencyResult resolvePlugins(List<Dependency> dependencies) {
        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(RUNTIME);
        DependencyResult dependencyResult = resolveDependencies(classpathFilter, dependencies);
        if (log.isDebugEnabled()) {
            logDependencyTree(dependencyResult.getRoot(), 0);
        }
        return dependencyResult;
    }

    private List<Dependency> getPluginDependencies(Plugin plugin) {
        List<Exclusion> exlusions = plugin.exclusions()
            .stream()
            .map(AetherArtifactProvider::getPluginExclusions)
            .flatMap(Collection::stream)
            .collect(toList());
        return plugin.artifactId()
            .stream()
            .map(artifactId -> getDependency(plugin, artifactId.trim(), exlusions))
            .collect(toList());
    }

    private static Dependency getDependency(Plugin plugin, String artifactId, List<Exclusion> exlusions) {
        return new Dependency(new DefaultArtifact(plugin.groupId(), artifactId, plugin.classifier()
            .orElse(null), plugin.type(), plugin.version()), RUNTIME, false, exlusions);
    }

    private static List<Exclusion> getPluginExclusions(com.buschmais.jqassistant.core.shared.configuration.Exclusion exclusion) {
        return exclusion.artifactId()
            .stream()
            .map(artifactId -> new Exclusion(exclusion.groupId(), artifactId.trim(), exclusion.classifier()
                .orElse(null), exclusion.type()))
            .collect(toList());
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

    private List<File> asFiles(DependencyResult dependencyResult) {
        return dependencyResult.getArtifactResults()
            .stream()
            .map(artifactResult -> artifactResult.getArtifact()
                .getFile())
            .collect(toList());
    }
}
