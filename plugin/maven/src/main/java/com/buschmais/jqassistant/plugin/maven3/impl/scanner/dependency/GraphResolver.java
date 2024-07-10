package com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency;

import java.util.*;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;

/**
 * A {@link DependencyNodeVisitor} implementation that is used for creating a
 * graph of all artifact dependencies.
 */
public class GraphResolver {

    Map<Artifact, Set<Artifact>> resolve(DependencyNode root, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor,
            ScannerContext context) {
        DependencyGraphVisitor visitor = new DependencyGraphVisitor();
        root.accept(visitor);
        Map<Artifact, Set<Artifact>> dependencies = visitor.getDependencies();
        createGraph(dependencies, root.getArtifact(), mainDescriptor, testDescriptor, context);
        return Collections.unmodifiableMap(dependencies);
    }

    private void createGraph(Map<Artifact, Set<Artifact>> dependencies, Artifact mainArtifact, MavenArtifactDescriptor mainArtifactDescriptor,
            MavenArtifactDescriptor testArtifactDescriptor, ScannerContext context) {
        for (Map.Entry<Artifact, Set<Artifact>> entry : dependencies.entrySet()) {
            Artifact dependentArtifact = entry.getKey();
            Set<Artifact> dependencyArtifacts = entry.getValue();
            for (Artifact dependencyArtifact : dependencyArtifacts) {
                MavenArtifactDescriptor artifactDescriptor = resolve(dependencyArtifact, context);
                if (dependentArtifact.equals(mainArtifact)) {
                    if (Artifact.SCOPE_TEST.equals(dependencyArtifact.getScope())) {
                        // Attach test scoped dependencies directly to the test artifact
                        testArtifactDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                    } else {
                        mainArtifactDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                    }
                } else {
                    MavenArtifactDescriptor dependentDescriptor = resolve(dependentArtifact, context);
                    dependentDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                }
            }
        }
    }

    private MavenArtifactDescriptor resolve(Artifact artifact, ScannerContext context) {
        ArtifactResolver artifactResolver = context.peek(ArtifactResolver.class);
        return artifactResolver.resolve(new MavenArtifactCoordinates(artifact, false), context);
    }

    /**
     * Visitor to build up a {@link Map} containing the dependencies per
     * {@link Artifact}.
     */
    private class DependencyGraphVisitor implements DependencyNodeVisitor {

        private final Deque<DependencyNode> stack = new LinkedList<>();

        private Map<Artifact, Set<Artifact>> dependencies = new LinkedHashMap<>();

        @Override
        public boolean visit(DependencyNode node) {
            stack.push(node);
            return true;
        }

        @Override
        public boolean endVisit(DependencyNode dependencyNode) {
            stack.pop();
            DependencyNode dependentNode = stack.peek();
            if (dependentNode != null) {
                dependencies.computeIfAbsent(dependentNode.getArtifact(), key -> new HashSet<>()).add(dependencyNode.getArtifact());
            }
            return true;
        }

        public Map<Artifact, Set<Artifact>> getDependencies() {
            return dependencies;
        }
    }
}
