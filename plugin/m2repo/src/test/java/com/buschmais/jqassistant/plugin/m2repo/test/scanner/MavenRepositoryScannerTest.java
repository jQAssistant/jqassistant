package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.apache.maven.model.Model;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.ArtifactProvider;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.EffectiveModelBuilderImpl;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenIndex;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepositoryScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.PomModelBuilder;

public class MavenRepositoryScannerTest {

    private static final String REPOSITORY = "jqa-test-repo";
    private static final String GROUP_ID = "com.jqassistant.m2repo.test";
    private static final String ARTIFACT_ID = "m2repo.test.module";
    private static final String VERSION_PREFIX = "1.";
    private static final String PACKAGING = "jar";

    private void buildWhenThenReturn(ArtifactProvider artifactProvider, final ArtifactInfo info) throws ArtifactResolutionException {
        doAnswer(new Answer<ArtifactResult>() {
            @Override
            public ArtifactResult answer(InvocationOnMock invocation) throws Throwable {
                Artifact a = (Artifact) invocation.getArguments()[0];
                File file = new File("test-repo/" + a.getGroupId() + "/" + a.getArtifactId() + "/" + a.getVersion() + "/" + a.getGroupId() + "-"
                        + a.getArtifactId() + "-" + a.getVersion() + "." + a.getExtension());
                Artifact artifact = a.setFile(file);
                ArtifactResult artifactResult = new ArtifactResult(new ArtifactRequest());
                artifactResult.setArtifact(artifact);
                return artifactResult;
            }
        }).when(artifactProvider).getArtifact(any(Artifact.class));
    }

    private List<ArtifactInfo> getTestArtifactInfos() {
        List<ArtifactInfo> infos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ArtifactInfo artifactInfo = new ArtifactInfo(REPOSITORY, GROUP_ID, ARTIFACT_ID, VERSION_PREFIX + i, null);
            artifactInfo.setFieldValue(MAVEN.PACKAGING, PACKAGING);
            infos.add(artifactInfo);
        }
        return infos;
    }

    @Test
    public void testMockMavenRepoScanner() throws Exception {
        MavenIndex mavenIndex = mock(MavenIndex.class);
        List<ArtifactInfo> testArtifactInfos = getTestArtifactInfos();
        when(mavenIndex.getArtifactsSince(new Date(0))).thenReturn(testArtifactInfos);

        ArtifactProvider artifactProvider = mock(ArtifactProvider.class);

        for (ArtifactInfo artifactInfo : testArtifactInfos) {
            buildWhenThenReturn(artifactProvider, artifactInfo);
        }

        PomModelBuilder pomModelBuilder = mock(EffectiveModelBuilderImpl.class);
        when(pomModelBuilder.getModel(any(File.class))).thenAnswer(new Answer<Model>() {
            @Override
            public Model answer(InvocationOnMock invocation) throws Throwable {
                Model model = mock(Model.class);
                when(model.getPackaging()).thenReturn("pom");
                return model;
            }
        });

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
        when(scanner.scan(any(FileResource.class), Mockito.contains("pom"), Mockito.any(Scope.class))).thenReturn(mock(MavenPomXmlDescriptor.class));
        when(store.addDescriptorType(any(Descriptor.class), eq(RepositoryArtifactDescriptor.class))).thenReturn(mock(RepositoryArtifactDescriptor.class));
        when(store.addDescriptorType(any(Descriptor.class), eq(MavenArtifactDescriptor.class))).thenReturn(mock(MavenArtifactDescriptor.class));

        MavenRepositoryScannerPlugin plugin = new MavenRepositoryScannerPlugin();
        plugin.configure(context, new HashMap<String, Object>());
        plugin.scanRepository(new URL(repoUrl), scanner, mavenIndex, artifactProvider);

        verify(mavenIndex).updateIndex();
        verify(store).find(MavenRepositoryDescriptor.class, repoUrl);
        verify(scanner, times(testArtifactInfos.size())).scan(any(ArtifactInfo.class), anyString(), eq(MavenScope.REPOSITORY));
    }
}
