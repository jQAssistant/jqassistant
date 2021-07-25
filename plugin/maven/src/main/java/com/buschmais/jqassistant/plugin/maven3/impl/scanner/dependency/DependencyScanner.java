package com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactFilter;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.xo.spi.reflection.DependencyResolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import static java.util.Collections.emptySet;

/**
 * Scanner for the resolved dependencies of a Maven project.
 */
public class DependencyScanner {

    private final GraphResolver graphResolver;

    public DependencyScanner(GraphResolver graphResolver) {
        this.graphResolver = graphResolver;
    }

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
     * @param scanner
     *            The Scanner.
     */
    public void evaluate(DependencyNode rootNode, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor, boolean scanDependencies,
            ArtifactFilter dependencyFilter, Scanner scanner) {
        Map<Artifact, Set<Artifact>> dependencies = resolveDependencyGraph(rootNode, mainDescriptor, testDescriptor, scanner.getContext());
        if (scanDependencies) {
            scanDependencyArtifacts(rootNode, dependencies, dependencyFilter, scanner);
        }
    }

    private Map<Artifact, Set<Artifact>> resolveDependencyGraph(DependencyNode rootNode, MavenArtifactDescriptor mainDescriptor,
            MavenArtifactDescriptor testDescriptor, ScannerContext context) {
        return graphResolver.resolve(rootNode, mainDescriptor, testDescriptor, context);
    }

    private void scanDependencyArtifacts(DependencyNode rootNode, Map<Artifact, Set<Artifact>> dependencies, ArtifactFilter dependencyFilter, Scanner scanner) {
        List<Artifact> artifacts = DependencyResolver.newInstance(dependencies.keySet(), artifact -> dependencies.getOrDefault(artifact, emptySet())).resolve();
        ArtifactResolver artifactResolver = scanner.getContext().peek(ArtifactResolver.class);
        for (Artifact artifact : artifacts) {
            // scan only dependencies, the root node represents the artifact to be created
            // by the current module and will be scanned separately.
            if (!artifact.equals(rootNode.getArtifact()) && dependencyFilter.match(artifact)) {
                File artifactFile = artifact.getFile();
                FileDescriptor fileDescriptor = artifactResolver.resolve(new MavenArtifactCoordinates(artifact, false), FileDescriptor.class,
                        scanner.getContext());
                // The dependency might have been scanned before within another module, so check
                // if it is not yet a FileContainerDescriptor (directory, JAR, etc.)
                if (artifactFile != null && !(fileDescriptor instanceof FileContainerDescriptor)) {
                    scanner.scan(artifactFile, artifactFile.getAbsolutePath(), DefaultScope.NONE);
                }
            }
        }
    }
}
