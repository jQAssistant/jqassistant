package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DependencyGraphResolverTest {

    @Mock
    private ArtifactResolver artifactResolver;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    private Map<String, MavenArtifactDescriptor> resolvedArtifacts = new HashMap<>();

    @Before
    public void setUp() {
        doReturn(store).when(context).getStore();
        doAnswer(i -> {
            Coordinates coordinates = (Coordinates) i.getArguments()[0];
            String fqn = MavenArtifactHelper.getId(coordinates);
            return resolvedArtifacts.computeIfAbsent(fqn, f -> {
                MavenArtifactDescriptor mavenArtifactDescriptor = mock(MavenArtifactDescriptor.class);
                MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, coordinates);
                return mavenArtifactDescriptor;
            });
        }).when(artifactResolver).resolve(any(Coordinates.class), eq(context));
        doAnswer(i -> mock(DependsOnDescriptor.class)).when(store).create(any(MavenArtifactDescriptor.class), any(), any(MavenArtifactDescriptor.class));
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
        verify(store).create(mainArtifact, DependsOnDescriptor.class, directDependency);
        verify(store, never()).create(mainArtifact, DependsOnDescriptor.class, transitiveDependency);
        verify(store, never()).create(mainArtifact, DependsOnDescriptor.class, testDependency);
        // Test artifact only depends directly on test dependency
        verify(store).create(testArtifact, DependsOnDescriptor.class, testDependency);
        verify(store, never()).create(testArtifact, DependsOnDescriptor.class, directDependency);
        verify(store, never()).create(testArtifact, DependsOnDescriptor.class, transitiveDependency);
        // Direct dependency only depends directly on transitive dependency
        verify(store).create(directDependency, DependsOnDescriptor.class, transitiveDependency);
        verify(store, never()).create(directDependency, DependsOnDescriptor.class, mainArtifact);
        verify(store, never()).create(directDependency, DependsOnDescriptor.class, testDependency);
        // Transitive dependency does not depend on anything else
        verify(store, never()).create(eq(transitiveDependency), eq(DependsOnDescriptor.class), any(MavenArtifactDescriptor.class));
    }

    private MavenArtifactDescriptor resolve(DependencyNode node, boolean testJar) {
        return artifactResolver.resolve(new MavenArtifactCoordinates(node.getArtifact(), testJar), context);
    }

    private DependencyNode getDependencyNode(DependencyNode parent, String artifactId, String scope) {
        DefaultArtifact artifact = new DefaultArtifact("com.acme", artifactId, "1.0.0", scope, "jar", null, mock(ArtifactHandler.class));
        return new DefaultDependencyNode(parent, artifact, null, null, null);
    }
}
