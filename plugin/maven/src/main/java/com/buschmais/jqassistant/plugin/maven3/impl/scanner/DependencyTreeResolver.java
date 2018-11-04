package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.shared.io.FileNameNormalizer;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.ArtifactCoordinates;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;

class DependencyTreeResolver implements DependencyNodeVisitor {

    private final MavenProject project;
    private final MavenArtifactDescriptor mainDescriptor;
    private final MavenArtifactDescriptor testDescriptor;
    private final ArtifactResolver artifactResolver;
    private final ScannerContext context;

    DependencyTreeResolver(MavenProject project, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor,
            ArtifactResolver artifactResolver, ScannerContext context) {
        this.project = project;
        this.mainDescriptor = mainDescriptor;
        this.testDescriptor = testDescriptor;
        this.artifactResolver = artifactResolver;
        this.context = context;
    }

    @Override
    public boolean visit(DependencyNode node) {
        Artifact artifact = node.getArtifact();
        MavenArtifactDescriptor artifactDescriptor;
        if (artifact.equals(project.getArtifact())) {
            artifactDescriptor = mainDescriptor;
        } else {
            artifactDescriptor = artifactResolver.resolve(new ArtifactCoordinates(artifact, false), context);
        }
        if (artifactDescriptor.getFileName() == null) {
            File artifactFile = artifact.getFile();
            if (artifactFile != null) {
                String fileName = FileNameNormalizer.normalize(artifactFile);
                artifactDescriptor.setFileName(fileName);
            }
            for (DependencyNode dependencyNode : node.getChildren()) {
                Artifact dependencyArtifact = dependencyNode.getArtifact();
                MavenArtifactDescriptor dependentDescriptor;
                if (artifact.equals(project.getArtifact())) {
                    if (testDescriptor != null && Artifact.SCOPE_TEST.equals(dependencyArtifact.getScope())) {
                        dependentDescriptor = testDescriptor;
                    } else {
                        dependentDescriptor = mainDescriptor;
                    }
                } else {
                    dependentDescriptor = artifactDescriptor;
                }
                MavenArtifactDescriptor dependencyDescriptor = artifactResolver.resolve(new ArtifactCoordinates(dependencyArtifact, false), context);
                DependsOnDescriptor dependsOnDescriptor = context.getStore().create(dependentDescriptor, DependsOnDescriptor.class, dependencyDescriptor);
                dependsOnDescriptor.setScope(dependencyArtifact.getScope());
                dependsOnDescriptor.setOptional(dependencyArtifact.isOptional());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean endVisit(DependencyNode node) {
        return true;
    }
}
