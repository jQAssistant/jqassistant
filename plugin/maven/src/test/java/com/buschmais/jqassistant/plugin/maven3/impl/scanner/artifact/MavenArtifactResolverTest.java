package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import java.util.function.Function;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.xo.api.Query;

import com.github.benmanes.caffeine.cache.Cache;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MavenArtifactResolverTest {

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private Store store;

    @Mock
    private Cache cache;

    private MavenArtifactResolver resolver = new MavenArtifactResolver();

    @Test
    public void resolveArtifact() {
        DefaultArtifact artifact = new DefaultArtifact("group", "artifact", "1.0.0", Artifact.SCOPE_COMPILE, "jar", "classifier", new DefaultArtifactHandler());
        doReturn(store).when(scannerContext).getStore();
        Query.Result result = mock(Query.Result.class);
        doReturn(true).when(result).hasResult();
        Query.Result.CompositeRowObject singleResult = mock(Query.Result.CompositeRowObject.class);
        doReturn(mock(MavenArtifactDescriptor.class)).when(singleResult).get("a", MavenArtifactDescriptor.class);
        doReturn(singleResult).when(result).getSingleResult();
        doReturn(result).when(store).executeQuery(anyString(), anyMap());
        doReturn(cache).when(store).getCache(anyString());
        doAnswer((Answer<MavenArtifactDescriptor>) invocation -> ((Function<String, MavenArtifactDescriptor>) invocation.getArgument(1))
                .apply(invocation.getArgument(0))).when(cache).get(anyString(), any(Function.class));

        MavenArtifactDescriptor artifactDescriptor = resolver.resolve(new MavenArtifactCoordinates(artifact, false), scannerContext);

        verify(artifactDescriptor).setGroup("group");
        verify(artifactDescriptor).setName("artifact");
        verify(artifactDescriptor).setVersion("1.0.0");
        verify(artifactDescriptor).setClassifier("classifier");
        verify(artifactDescriptor).setType("jar");
    }

}
