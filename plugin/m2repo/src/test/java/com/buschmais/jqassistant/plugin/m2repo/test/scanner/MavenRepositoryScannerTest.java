package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.ArtifactFilter;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.ArtifactProvider;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenIndex;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepositoryScannerPlugin;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

public class MavenRepositoryScannerTest {

    private static final String REPOSITORY = "jqa-test-repo";
    private static final String GROUP_ID = "com.jqassistant.m2repo.test";
    private static final String ARTIFACT_ID = "m2repo.test.module";
    private static final String VERSION_PREFIX = "1.";
    private static final String PACKAGING = "jar";

    private void buildWhenThenReturn(ArtifactProvider artifactProvider, ArtifactInfo info) throws ArtifactResolutionException {
        when(artifactProvider.getArtifact(any(Artifact.class))).thenReturn(newArtifactResult(info));
    }

    private Iterable<ArtifactInfo> getTestArtifactInfos() {
        List<ArtifactInfo> infos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ArtifactInfo artifactInfo = new ArtifactInfo(REPOSITORY, GROUP_ID, ARTIFACT_ID, VERSION_PREFIX + i, null);
            artifactInfo.setFieldValue(MAVEN.PACKAGING, PACKAGING);
            infos.add(artifactInfo);
        }
        return infos;
    }

    private ArtifactResult newArtifactResult(ArtifactInfo info) {
        ArtifactResult result = new ArtifactResult(new ArtifactRequest());
        Artifact artifact = new DefaultArtifact(info.groupId, info.artifactId, info.packaging, info.version);
        result.setArtifact(artifact.setFile(newFile(info)));
        return result;
    }

    private File newFile(ArtifactInfo info) {
        return new File("test-repo/" + info.groupId + "/" + info.artifactId + "/" + info.version + "/" + info.groupId + "-" + info.artifactId + "-"
                + info.version + "." + info.packaging);
    }

    @Test
    public void testMockMavenRepoScanner() throws Exception {
        MavenIndex mavenIndex = mock(MavenIndex.class);
        Iterable<ArtifactInfo> testArtifactInfos = getTestArtifactInfos();
        when(mavenIndex.getArtifactsSince(new Date(0))).thenReturn(testArtifactInfos);

        ArtifactProvider artifactProvider = mock(ArtifactProvider.class);

        for (ArtifactInfo artifactInfo : testArtifactInfos) {
            buildWhenThenReturn(artifactProvider, artifactInfo);
        }
        final String repoUrl = "http://example.com/m2repo";

        Store store = mock(Store.class);
        ScannerContext context = mock(ScannerContext.class);
        when(context.getStore()).thenReturn(store);
        Scanner scanner = mock(Scanner.class);
        when(scanner.getContext()).thenReturn(context);

        MavenRepositoryDescriptor repoDescriptor = mock(MavenRepositoryDescriptor.class);
        when(store.find(MavenRepositoryDescriptor.class, repoUrl)).thenReturn(repoDescriptor);
        when(repoDescriptor.getArtifact(anyString())).thenReturn(null);

        when(scanner.scan(any(FileResource.class), anyString(), Mockito.any(Scope.class))).thenReturn(mock(FileDescriptor.class));
        when(store.addDescriptorType(any(Descriptor.class), eq(RepositoryArtifactDescriptor.class))).thenReturn(mock(RepositoryArtifactDescriptor.class));

        Result<CompositeRowObject> queryResult = mock(Result.class);
        when(queryResult.hasResult()).thenReturn(false);
        when(
                store.executeQuery(
                        Matchers.eq("MATCH (n:RepositoryArtifact)<-[:CONTAINS_ARTIFACT]-(m:Maven:Repository) WHERE n.mavenCoordinates={coords} AND n.lastModified<>{lastModified} and m.url={url} RETURN n"),
                        Matchers.anyMap())).thenReturn(queryResult);

        ArtifactFilter artifactFilter = mock(ArtifactFilter.class);
        when(artifactFilter.match(any(org.apache.maven.artifact.Artifact.class))).thenReturn(true);

        MavenRepositoryScannerPlugin plugin = new MavenRepositoryScannerPlugin();
        plugin.configure(new HashMap<String, Object>());
        plugin.scanRepository(new URL(repoUrl), scanner, mavenIndex, artifactProvider, artifactFilter);

        verify(mavenIndex).updateIndex();
        verify(store).find(MavenRepositoryDescriptor.class, repoUrl);
        verify(artifactFilter, Mockito.times(3)).match(any(org.apache.maven.artifact.Artifact.class));
        verify(store, new Times(6)).addDescriptorType(any(Descriptor.class), eq(RepositoryArtifactDescriptor.class));
    }
}
