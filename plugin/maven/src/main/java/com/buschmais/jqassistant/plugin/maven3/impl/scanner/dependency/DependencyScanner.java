package com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenRepositoryArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenRepositoryFileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenRepositoryResolver;
import com.buschmais.xo.spi.reflection.DependencyResolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;

/**
 * Scanner for the resolved dependencies of a Maven project.
 */
public class DependencyScanner {

    public static final String PROPERTY_ENABLED = "jqassistant.plugin.maven3.scanDependencies";

    /**
     * Scan the resolved dependencies of the project.
     *
     * @param rootNode
     *            The root node of the dependency tree.
     * @param mainDescriptor
     *            The {@link MavenArtifactDescriptor} representing the main
     *            artifact.
     * @param testDescriptor
     *            The {@link MavenArtifactDescriptor} representing the test
     *            artifact.
     * @param localRepositoryDirectory
     *            The base directory of the local Maven repository.
     * @param scanner
     *            The Scanner.
     */
    public void evaluate(DependencyNode rootNode, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor, File localRepositoryDirectory,
            Scanner scanner) {
        ScannerContext context = scanner.getContext();
        MavenRepositoryDescriptor mavenRepositoryDescriptor = MavenRepositoryResolver.resolve(context.getStore(), localRepositoryDirectory.getAbsolutePath());
        MavenRepositoryFileResolver repositoryFileResolver = new MavenRepositoryFileResolver(mavenRepositoryDescriptor);
        MavenRepositoryArtifactResolver repositoryArtifactResolver = new MavenRepositoryArtifactResolver(localRepositoryDirectory, repositoryFileResolver);
        context.push(ArtifactResolver.class, repositoryArtifactResolver);
        context.push(FileResolver.class, repositoryFileResolver);
        try {
            Map<Artifact, Set<Artifact>> dependencies = resolveDependencyGraph(rootNode, mainDescriptor, testDescriptor, repositoryArtifactResolver, context);
            if (System.getProperty(PROPERTY_ENABLED) != null) {
                scanArtifacts(dependencies, repositoryArtifactResolver, scanner);
            }
        } finally {
            context.pop(FileResolver.class);
            context.pop(ArtifactResolver.class);
        }
    }

    private Map<Artifact, Set<Artifact>> resolveDependencyGraph(DependencyNode rootNode, MavenArtifactDescriptor mainDescriptor,
            MavenArtifactDescriptor testDescriptor, MavenRepositoryArtifactResolver repositoryArtifactResolver, ScannerContext context) {
        GraphResolver graphResolver = new GraphResolver(repositoryArtifactResolver, context);
        return graphResolver.resolve(rootNode, mainDescriptor, testDescriptor);
    }

    private void scanArtifacts(Map<Artifact, Set<Artifact>> dependencies, ArtifactResolver artifactResolver, Scanner scanner) {
        List<Artifact> artifacts = DependencyResolver
                .newInstance(dependencies.keySet(), artifact -> dependencies.getOrDefault(artifact, Collections.emptySet())).resolve();
        for (Artifact artifact : artifacts) {
            File artifactFile = artifact.getFile();
            MavenArtifactDescriptor artifactDescriptor = artifactResolver.resolve(new MavenArtifactCoordinates(artifact, false), scanner.getContext());
            if (artifactFile != null && !(artifactDescriptor instanceof FileContainerDescriptor)) {
                scanner.scan(artifactFile, artifactFile.getAbsolutePath(), DefaultScope.NONE);
            }
        }
    }
}
