package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.Deque;
import java.util.LinkedList;

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
class DependencyGraphResolver implements DependencyNodeVisitor {

    private final MavenArtifactDescriptor mainDescriptor;
    private final MavenArtifactDescriptor testDescriptor;
    private final ArtifactResolver artifactResolver;
    private final ScannerContext context;

    private final Deque<MavenArtifactDescriptor> dependencies = new LinkedList<>();

    DependencyGraphResolver(MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor, ArtifactResolver artifactResolver,
            ScannerContext context) {
        this.mainDescriptor = mainDescriptor;
        this.testDescriptor = testDescriptor;
        this.artifactResolver = artifactResolver;
        this.context = context;
    }

    @Override
    public boolean visit(DependencyNode node) {
        dependencies.push(getMavenArtifactDescriptor(node));
        return true;
    }

    @Override
    public boolean endVisit(DependencyNode dependencyNode) {
        dependencies.pop();
        MavenArtifactDescriptor dependentDescriptor = dependencies.peek();
        if (dependentDescriptor != null) {
            // Attach test scoped dependencies of the main artifact directly to the test artifact
            Artifact dependencyArtifact = dependencyNode.getArtifact();
            if (Artifact.SCOPE_TEST.equals(dependencyArtifact.getScope()) && dependentDescriptor.equals(mainDescriptor)) {
                dependentDescriptor = testDescriptor;
            }
            MavenArtifactDescriptor artifactDescriptor = getMavenArtifactDescriptor(dependencyNode);
            // Merge the dependency to avoid duplicates as in multi-module projects the same
            // dependency may be reported multiple times, e.g. for
            // module2 -> module1
            // module3 -> module1
            // module3 -> module2
            // the dependency of module2 -> module1 will be reported for module2 & module3
            dependentDescriptor.addDependency(artifactDescriptor, dependencyArtifact.getScope(), dependencyArtifact.isOptional());
        }
        return true;
    }

    private MavenArtifactDescriptor getMavenArtifactDescriptor(DependencyNode node) {
        return artifactResolver.resolve(new MavenArtifactCoordinates(node.getArtifact(), false), context);
    }

}
