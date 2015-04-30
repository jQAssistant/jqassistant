package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.*;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
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

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MavenRepositoryScannerTest {

    private static final String REPOSITORY = "jqa-test-repo";
    private static final String GROUP_ID = "com.jqassistant.m2repo.test";
    private static final String ARTIFACT_ID_PREFIX = "m2repo.test.module";
    private static final String VERSION_PREFIX = "1.";
    private static final String CLASSIFIER = "jar";
    private static final String PACKAGING = "jar";

    private void buildWhenThenReturn(ArtifactProvider artifactProvider, ArtifactInfo info) throws ArtifactResolutionException {
        when(artifactProvider.downloadArtifact(any(Artifact.class))).thenReturn(newArtifactResultList(info));
    }

    private Iterable<ArtifactInfo> getTestArtifactInfos() {
        List<ArtifactInfo> infos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ArtifactInfo artifactInfo = new ArtifactInfo(REPOSITORY, GROUP_ID, ARTIFACT_ID_PREFIX + i, VERSION_PREFIX + i, CLASSIFIER);
            artifactInfo.setFieldValue(MAVEN.PACKAGING, PACKAGING);
            infos.add(artifactInfo);
        }

        return infos;
    }

    private List<ArtifactResult> newArtifactResultList(ArtifactInfo info) {
        ArtifactResult result = new ArtifactResult(new ArtifactRequest());
        Artifact artifact = new DefaultArtifact(info.groupId, info.artifactId, info.packaging, info.version);
        result.setArtifact(artifact.setFile(newFile(info)));
        return Arrays.asList(result);
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

        CompositeRowObject rowObject = mock(CompositeRowObject.class);
        when(rowObject.get("nodeCount", Long.class)).thenReturn(0L);

        Result<CompositeRowObject> inDbQueryResult = mock(Result.class);
        when(inDbQueryResult.getSingleResult()).thenReturn(rowObject);

        Store store = mock(Store.class);
        when(
                store.executeQuery(
                        Matchers.eq("MATCH (n:RepositoryArtifact)<-[:CONTAINS_ARTIFACT]-(m:Maven:Repository) WHERE n.mavenCoordinates={coords} and n.lastModified={lastModified} and m.url={url} RETURN count(n) as nodeCount;"),
                        Matchers.anyMap())).thenReturn(inDbQueryResult);

        ScannerContext context = mock(ScannerContext.class);
        when(context.getStore()).thenReturn(store);
        MavenRepositoryDescriptor repoDescriptor = mock(MavenRepositoryDescriptor.class);
        when(store.find(MavenRepositoryDescriptor.class, repoUrl)).thenReturn(repoDescriptor);

        Scanner scanner = mock(Scanner.class);
        when(scanner.getContext()).thenReturn(context);

        Descriptor descriptor = mock(Descriptor.class);
        RepositoryArtifactDescriptor artifactDescriptor = mock(RepositoryArtifactDescriptor.class);
        when(store.addDescriptorType(descriptor, RepositoryArtifactDescriptor.class)).thenReturn(artifactDescriptor);

        Result<CompositeRowObject> queryResult = mock(Result.class);
        when(queryResult.hasResult()).thenReturn(false);
        when(
                store.executeQuery(
                        Matchers.eq("MATCH (n:RepositoryArtifact)<-[:CONTAINS_ARTIFACT]-(m:Maven:Repository) WHERE n.mavenCoordinates={coords} AND n.lastModified<>{lastModified} and m.url={url} RETURN n"),
                        Matchers.anyMap())).thenReturn(queryResult);

        for (ArtifactInfo artifactInfo : testArtifactInfos) {
            File artifactFile = newFile(artifactInfo);
            when(scanner.scan(new DefaultFileResource(artifactFile), artifactFile.getAbsolutePath(), null)).thenReturn(descriptor);
        }

        ArtifactFilter artifactFilter = mock(ArtifactFilter.class);
        when(artifactFilter.match(any(org.apache.maven.artifact.Artifact.class))).thenReturn(true);

        MavenRepositoryScannerPlugin plugin = new MavenRepositoryScannerPlugin();
        plugin.configure(new HashMap<String, Object>());
        plugin.scanRepository(new URL(repoUrl), scanner, mavenIndex, artifactProvider, artifactFilter);
        verify(mavenIndex).updateIndex();
        verify(artifactFilter, Mockito.times(3)).match(any(org.apache.maven.artifact.Artifact.class));
        verify(store).find(MavenRepositoryDescriptor.class, repoUrl);
        verify(store, new Times(3)).addDescriptorType(descriptor, RepositoryArtifactDescriptor.class);
    }
}
