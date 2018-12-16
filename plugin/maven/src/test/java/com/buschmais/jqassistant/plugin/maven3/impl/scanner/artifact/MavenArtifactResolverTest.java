package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.xo.api.Query;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MavenArtifactResolverTest {

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private Store store;

    private MavenArtifactResolver resolver = new MavenArtifactResolver();

    @BeforeEach
    public void setUp() {
        doReturn(store).when(scannerContext).getStore();
    }

    @Test
    public void nonExistingArtifact() {
        DefaultArtifact artifact = new DefaultArtifact("group", "artifact", "1.0.0", Artifact.SCOPE_COMPILE, "jar", "classifier", new DefaultArtifactHandler());
        Query.Result result = mock(Query.Result.class);
        doReturn(false).when(result).hasResult();
        doReturn(result).when(store).executeQuery(anyString(), anyMap());
        doAnswer((Answer<MavenArtifactDescriptor>) invocation -> {
            MavenArtifactDescriptor artifactDescriptor = mock(MavenArtifactDescriptor.class);
            artifactDescriptor.setFullQualifiedName((String) invocation.getArguments()[1]);
            return artifactDescriptor;
        }).when(store).create(any(), anyString());

        MavenArtifactDescriptor artifactDescriptor = resolver.resolve(new MavenArtifactCoordinates(artifact, false), scannerContext);

        verify(store).create(MavenArtifactDescriptor.class, "group:artifact:jar:classifier:1.0.0");
        verify(artifactDescriptor).setFullQualifiedName("group:artifact:jar:classifier:1.0.0");
        verify(artifactDescriptor).setGroup("group");
        verify(artifactDescriptor).setName("artifact");
        verify(artifactDescriptor).setVersion("1.0.0");
        verify(artifactDescriptor).setClassifier("classifier");
        verify(artifactDescriptor).setType("jar");
    }

    @Test
    public void existingArtifact() {
        DefaultArtifact artifact = new DefaultArtifact("group", "artifact", "1.0.0", Artifact.SCOPE_COMPILE, "jar", "classifier", new DefaultArtifactHandler());
        MavenArtifactDescriptor artifactDescriptor = mock(MavenArtifactDescriptor.class);
        artifactDescriptor.setFullQualifiedName("group:artifact:jar:classifier:1.0.0");
        Query.Result result = mock(Query.Result.class);
        doReturn(true).when(result).hasResult();
        Query.Result.CompositeRowObject singleResult = mock(Query.Result.CompositeRowObject.class);
        doReturn(artifactDescriptor).when(singleResult).get("a", MavenArtifactDescriptor.class);
        doReturn(singleResult).when(result).getSingleResult();
        doReturn(result).when(store).executeQuery(anyString(), anyMap());

        for (int i = 0; i < 2; i++) {
            MavenArtifactDescriptor resultDescriptor = resolver.resolve(new MavenArtifactCoordinates(artifact, false), scannerContext);

            assertThat(resultDescriptor, is(artifactDescriptor));
            verify(store, never()).create(MavenArtifactDescriptor.class, "group:artifact:jar:classifier:1.0.0");
            verify(resultDescriptor, never()).setGroup(anyString());
            verify(resultDescriptor, never()).setName(anyString());
            verify(resultDescriptor, never()).setVersion(anyString());
            verify(resultDescriptor, never()).setClassifier(anyString());
            verify(resultDescriptor, never()).setType(anyString());
        }

        verify(store, atMost(1)).executeQuery(anyString(), anyMap());

    }
}
