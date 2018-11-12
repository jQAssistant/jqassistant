package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.Deque;
import java.util.LinkedList;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
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
            if (Artifact.SCOPE_TEST.equals(dependencyNode.getArtifact().getScope()) && dependentDescriptor.equals(mainDescriptor)) {
                dependentDescriptor = testDescriptor;
            }
            MavenArtifactDescriptor artifactDescriptor = getMavenArtifactDescriptor(dependencyNode);
            DependsOnDescriptor dependsOnDescriptor = context.getStore().create(dependentDescriptor, DependsOnDescriptor.class, artifactDescriptor);
            dependsOnDescriptor.setScope(dependencyNode.getArtifact().getScope());
            dependsOnDescriptor.setOptional(dependencyNode.getArtifact().isOptional());
        }
        return true;
    }

    private MavenArtifactDescriptor getMavenArtifactDescriptor(DependencyNode node) {
        return artifactResolver.resolve(new MavenArtifactCoordinates(node.getArtifact(), false), context);
    }

}
