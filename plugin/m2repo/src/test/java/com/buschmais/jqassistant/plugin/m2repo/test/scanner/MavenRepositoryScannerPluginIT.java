package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.junit.Test;
import org.mockito.internal.verification.Times;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.m2repo.api.model.ContainsArtifactDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.ArtifactResolver;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.DefaultFileResource;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenIndex;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepoCredentials;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepositoryScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    private static final String REPOSITORY = "jqa-test-repo";
    private static final String GROUP_ID = "com.jqassistant.m2repo.test";
    private static final String ARTIFACT_ID_PREFIX = "m2repo.test.module";
    private static final String VERSION_PREFIX = "1.";
    private static final String CLASSIFIER = "jar";
    private static final String PACKAGING = "jar";

    private void buildWhenThenReturn(ArtifactResolver artifactResolver, ArtifactInfo info) throws ArtifactResolutionException {
        when(artifactResolver.downloadArtifact(info.groupId, info.artifactId, info.packaging, info.version)).thenReturn(newFile(info));
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

    private File newFile(ArtifactInfo info) {
        return new File("test-repo/" + info.groupId + "/" + info.artifactId + "/" + info.version + "/" + info.groupId + "-" + info.artifactId + "-"
                + info.version + "." + info.packaging);
    }

    // @Test
    public void testMavenRepoScannerRealRepo() throws MalformedURLException {
        try {
            long beginn = System.currentTimeMillis();
            store.beginTransaction();
            getScanner().scan(new URL(MavenRepoCredentials.REPO_URL), MavenRepoCredentials.REPO_URL, MavenScope.REPOSITORY);
            System.out.println("Dauer: " + (System.currentTimeMillis() - beginn));
        } finally {
            store.commitTransaction();
        }
    }

    @Test
    public void testMockMavenRepoScanner() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);

        MavenIndex mavenIndex = mock(MavenIndex.class);
        when(mavenIndex.getLastUpdateLocalRepo()).thenReturn(cal.getTime());

        Iterable<ArtifactInfo> testArtifactInfos = getTestArtifactInfos();
        when(mavenIndex.getArtifactsSince(cal.getTime())).thenReturn(testArtifactInfos);

        ArtifactResolver artifactResolver = mock(ArtifactResolver.class);

        for (ArtifactInfo artifactInfo : testArtifactInfos) {
            buildWhenThenReturn(artifactResolver, artifactInfo);
        }
        final String repoUrl = "http://example.com/m2repo";

        Store store = mock(Store.class);
        ScannerContext context = mock(ScannerContext.class);
        when(context.getStore()).thenReturn(store);
        MavenRepositoryDescriptor repoDescriptor = mock(MavenRepositoryDescriptor.class);
        when(store.find(MavenRepositoryDescriptor.class, repoUrl)).thenReturn(repoDescriptor);

        Scanner scanner = mock(Scanner.class);
        when(scanner.getContext()).thenReturn(context);

        Descriptor descriptor = mock(Descriptor.class);
        RepositoryArtifactDescriptor artifactDescriptor = mock(RepositoryArtifactDescriptor.class);
        when(store.migrate(descriptor, RepositoryArtifactDescriptor.class)).thenReturn(artifactDescriptor);
        ContainsArtifactDescriptor containsArtifactDescriptor = mock(ContainsArtifactDescriptor.class);
        when(store.create(repoDescriptor, ContainsArtifactDescriptor.class, artifactDescriptor)).thenReturn(containsArtifactDescriptor);

        for (ArtifactInfo artifactInfo : testArtifactInfos) {
            File artifactFile = newFile(artifactInfo);
            when(scanner.scan(new DefaultFileResource(artifactFile), artifactFile.getAbsolutePath(), null)).thenReturn(descriptor);
        }

        MavenRepositoryScannerPlugin plugin = new MavenRepositoryScannerPlugin(mavenIndex, artifactResolver);
        plugin.scan(new URL(repoUrl), repoUrl, MavenScope.REPOSITORY, scanner);
        verify(mavenIndex).updateIndex(anyString(), anyString());
        verify(store).find(MavenRepositoryDescriptor.class, repoUrl);
        verify(store, new Times(3)).migrate(descriptor, RepositoryArtifactDescriptor.class);
        verify(store, new Times(3)).create(repoDescriptor, ContainsArtifactDescriptor.class, artifactDescriptor);
    }
}
