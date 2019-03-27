package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactHelper;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DependencyGraphResolverTest {

    @Mock
    private ArtifactResolver artifactResolver;

    @Mock
    private ScannerContext context;

    private Map<String, MavenArtifactDescriptor> resolvedArtifacts = new HashMap<>();

    @BeforeEach
    public void setUp() {
        doAnswer(i -> {
            Coordinates coordinates = (Coordinates) i.getArguments()[0];
            String fqn = MavenArtifactHelper.getId(coordinates);
            return resolvedArtifacts.computeIfAbsent(fqn, f -> {
                MavenArtifactDescriptor mavenArtifactDescriptor = mock(MavenArtifactDescriptor.class);
                MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, coordinates);
                return mavenArtifactDescriptor;
            });
        }).when(artifactResolver).resolve(any(Coordinates.class), eq(context));
    }

    @Test
    public void resolve() {
        DependencyNode mainNode = getDependencyNode(null, "main", Artifact.SCOPE_COMPILE);
        DependencyNode directDependencyNode = getDependencyNode(mainNode, "direct-dependency", Artifact.SCOPE_COMPILE);
        DependencyNode transitiveDependencyNode = getDependencyNode(directDependencyNode, "transitive-dependency", Artifact.SCOPE_COMPILE);
        DependencyNode testDependencyNode = getDependencyNode(mainNode, "test-dependency", Artifact.SCOPE_TEST);
        MavenArtifactDescriptor mainArtifact = resolve(mainNode, false);
        MavenArtifactDescriptor testArtifact = resolve(mainNode, true);
        DependencyGraphResolver resolver = new DependencyGraphResolver(mainArtifact, testArtifact, artifactResolver, context);

        // Simulate the following dependency graph:
        // mainNode -(compile)-> directDependencyNode -(compile)->
        // transitiveDependencyNode
        // -(test)-> testDependencyNode
        resolver.visit(mainNode);
        resolver.visit(directDependencyNode);
        resolver.visit(transitiveDependencyNode);
        resolver.endVisit(transitiveDependencyNode);
        resolver.endVisit(directDependencyNode);
        resolver.visit(testDependencyNode);
        resolver.endVisit(testDependencyNode);
        resolver.endVisit(mainNode);

        MavenArtifactDescriptor directDependency = resolve(directDependencyNode, false);
        MavenArtifactDescriptor transitiveDependency = resolve(transitiveDependencyNode, false);
        MavenArtifactDescriptor testDependency = resolve(testDependencyNode, false);

        // Main artifact only depends directly on direct dependency
        verify(mainArtifact).addDependency(directDependency, Artifact.SCOPE_COMPILE, false);
        verify(mainArtifact, never()).addDependency(eq(transitiveDependency), anyString(), anyBoolean());
        verify(mainArtifact, never()).addDependency(eq(testDependency), anyString(), anyBoolean());
        // Test artifact only depends directly on test dependency
        verify(testArtifact).addDependency(testDependency, Artifact.SCOPE_TEST, false);
        verify(testArtifact, never()).addDependency(eq(directDependency), anyString(), anyBoolean());
        verify(testArtifact, never()).addDependency(eq(transitiveDependency), anyString(), anyBoolean());
        // Direct dependency only depends directly on transitive dependency
        verify(directDependency).addDependency(transitiveDependency, Artifact.SCOPE_COMPILE, false);
        verify(directDependency, never()).addDependency(eq(mainArtifact), anyString(), anyBoolean());
        verify(directDependency, never()).addDependency(eq(testDependency), anyString(), anyBoolean());
        // Transitive dependency does not depend on anything else
        verify(transitiveDependency, never()).addDependency(any(MavenArtifactDescriptor.class), anyString(), anyBoolean());
    }

    private MavenArtifactDescriptor resolve(DependencyNode node, boolean testJar) {
        return artifactResolver.resolve(new MavenArtifactCoordinates(node.getArtifact(), testJar), context);
    }

    private DependencyNode getDependencyNode(DependencyNode parent, String artifactId, String scope) {
        DefaultArtifact artifact = new DefaultArtifact("com.acme", artifactId, "1.0.0", scope, "jar", null, mock(ArtifactHandler.class));
        return new DefaultDependencyNode(parent, artifact, null, null, null);
    }
}
