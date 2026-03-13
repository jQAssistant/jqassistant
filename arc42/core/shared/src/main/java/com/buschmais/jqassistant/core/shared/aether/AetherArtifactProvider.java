package com.buschmais.jqassistant.core.shared.aether;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.core.shared.aether.configuration.Plugin;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;

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
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import static java.util.stream.Collectors.toList;

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
            .map(this::toDependency)
            .collect(toList());
    }

    private DependencyResult resolvePlugins(List<Dependency> dependencies) {
        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME);
        DependencyResult dependencyResult = resolveDependencies(classpathFilter, dependencies);
        if (log.isDebugEnabled()) {
            logDependencyTree(dependencyResult.getRoot(), 0);
        }
        return dependencyResult;
    }

    private Dependency toDependency(Plugin plugin) {
        List<Exclusion> exclusions = plugin.exclusions()
            .stream()
            .map(exclusion -> new Exclusion(exclusion.groupId(), exclusion.artifactId()
                .trim(), exclusion.classifier()
                .orElse(null), exclusion.type()))
            .collect(toList());
        return new Dependency(new DefaultArtifact(plugin.groupId(), plugin.artifactId()
            .trim(), plugin.classifier()
            .orElse(null), plugin.type(), plugin.version()), JavaScopes.RUNTIME, false, exclusions);
    }

    private DependencyResult resolveDependencies(DependencyFilter classpathFilter, List<Dependency> dependencies) {
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setDependencies(dependencies);
        collectRequest.setRepositories(repositories);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);
        try {
            return repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        } catch (DependencyResolutionException e) {
            throw new IllegalStateException("Cannot resolve plugin dependencies", e);
        }
    }

    private void logDependencyTree(DependencyNode node, int indent) {
        StringBuilder builder = new StringBuilder();
        builder.append(" ".repeat(indent));
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
