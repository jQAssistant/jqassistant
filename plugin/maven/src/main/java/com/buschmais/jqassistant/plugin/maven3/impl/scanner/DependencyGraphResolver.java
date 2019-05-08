package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

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
class DependencyGraphResolver {

    private final ArtifactResolver artifactResolver;
    private final ScannerContext context;

    DependencyGraphResolver(ArtifactResolver artifactResolver, ScannerContext context) {
        this.artifactResolver = artifactResolver;
        this.context = context;
    }

    public void resolve(DependencyNode root, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor) {
        DependencyGraphVisitor visitor = new DependencyGraphVisitor();
        root.accept(visitor);
        Map<Artifact, Set<Artifact>> dependencies = visitor.getDependencies();
        createGraph(dependencies, mainDescriptor, testDescriptor);
    }

    private void createGraph(Map<Artifact, Set<Artifact>> dependencies, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor) {
        for (Map.Entry<Artifact, Set<Artifact>> entry : dependencies.entrySet()) {
            Artifact dependentArtifact = entry.getKey();
            Set<Artifact> dependencyArtifacts = entry.getValue();
            MavenArtifactDescriptor dependentDescriptor = getMavenArtifactDescriptor(dependentArtifact);
            for (Artifact dependencyArtifact : dependencyArtifacts) {
                MavenArtifactDescriptor artifactDescriptor = getMavenArtifactDescriptor(dependencyArtifact);
                if (Artifact.SCOPE_TEST.equals(dependencyArtifact.getScope()) && dependentDescriptor.equals(mainDescriptor)) {
                    // Attach test scoped dependencies directly to the test artifact
                    testDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                } else {
                    dependentDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                }
            }
        }
    }

    private MavenArtifactDescriptor getMavenArtifactDescriptor(Artifact artifact) {
        return artifactResolver.resolve(new MavenArtifactCoordinates(artifact, false), context);
    }

    private class DependencyGraphVisitor implements DependencyNodeVisitor {

        private final Deque<DependencyNode> stack = new LinkedList<>();

        private Map<Artifact, Set<Artifact>> dependencies = new LinkedHashMap<>();

        @Override
        public boolean visit(DependencyNode node) {
            Artifact artifact = node.getArtifact();
            if (!dependencies.containsKey(artifact)) {
                dependencies.put(artifact, new HashSet<>());
            }
            stack.push(node);
            return true;
        }

        @Override
        public boolean endVisit(DependencyNode dependencyNode) {
            stack.pop();
            DependencyNode dependentNode = stack.peek();
            if (dependentNode != null) {
                dependencies.get(dependentNode.getArtifact()).add(dependencyNode.getArtifact());
            }
            return true;
        }

        public Map<Artifact, Set<Artifact>> getDependencies() {
            return dependencies;
        }
    }

}
