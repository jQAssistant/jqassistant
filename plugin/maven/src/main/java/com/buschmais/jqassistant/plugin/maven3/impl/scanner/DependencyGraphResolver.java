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
        Map<DependencyNode, Set<DependencyNode>> dependencies = visitor.getDependencies();
        createGraph(dependencies, mainDescriptor, testDescriptor);
    }

    private void createGraph(Map<DependencyNode, Set<DependencyNode>> dependencies, MavenArtifactDescriptor mainDescriptor,
            MavenArtifactDescriptor testDescriptor) {
        for (Map.Entry<DependencyNode, Set<DependencyNode>> entry : dependencies.entrySet()) {
            DependencyNode dependentNode = entry.getKey();
            Set<DependencyNode> dependencyNodes = entry.getValue();
            MavenArtifactDescriptor dependentDescriptor = getMavenArtifactDescriptor(dependentNode);
            for (DependencyNode dependencyNode : dependencyNodes) {
                Artifact dependencyArtifact = dependencyNode.getArtifact();
                MavenArtifactDescriptor artifactDescriptor = getMavenArtifactDescriptor(dependencyNode);
                if (Artifact.SCOPE_TEST.equals(dependencyArtifact.getScope()) && dependentDescriptor.equals(mainDescriptor)) {
                    // Attach test scoped dependencies directly to the test artifact
                    testDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                } else {
                    dependentDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
                }
            }
        }
    }

    private MavenArtifactDescriptor getMavenArtifactDescriptor(DependencyNode node) {
        return artifactResolver.resolve(new MavenArtifactCoordinates(node.getArtifact(), false), context);
    }

    private class DependencyGraphVisitor implements DependencyNodeVisitor {

        private final Deque<DependencyNode> stack = new LinkedList<>();

        private Map<DependencyNode, Set<DependencyNode>> dependencies = new HashMap<>();

        @Override
        public boolean visit(DependencyNode node) {
            if (!dependencies.containsKey(node)) {
                stack.push(node);
                return true;
            }
            return false;
        }

        @Override
        public boolean endVisit(DependencyNode dependencyNode) {
            stack.pop();
            DependencyNode dependentDescriptor = stack.peek();
            if (dependentDescriptor != null) {
                Set<DependencyNode> dependencyNodes = dependencies.computeIfAbsent(dependentDescriptor, k -> new HashSet<>());
                dependencyNodes.add(dependencyNode);
            }
            return true;
        }

        public Map<DependencyNode, Set<DependencyNode>> getDependencies() {
            return dependencies;
        }
    }

}
