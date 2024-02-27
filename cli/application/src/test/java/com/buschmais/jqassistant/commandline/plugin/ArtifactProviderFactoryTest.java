package com.buschmais.jqassistant.commandline.plugin;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.configuration.Mirror;
import com.buschmais.jqassistant.commandline.configuration.Proxy;
import com.buschmais.jqassistant.commandline.configuration.Repositories;
import com.buschmais.jqassistant.core.runtime.impl.plugin.AetherArtifactProvider;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;

import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.commandline.plugin.ArtifactProviderFactory.MAVEN_CENTRAL_ID;
import static com.buschmais.jqassistant.commandline.plugin.ArtifactProviderFactory.MAVEN_CENTRAL_URL;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ArtifactProviderFactoryTest {

    private ArtifactProviderFactory artifactProviderFactory;

    @Mock
    private CliConfiguration configuration;

    @Mock
    private Repositories repositories;

    @BeforeEach
    void setUp() {
        doReturn(repositories).when(configuration)
            .repositories();
        URL userHomeUrl = ArtifactProviderFactoryTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());
        this.artifactProviderFactory = new ArtifactProviderFactory(userHome);
    }

    @Test
    void mavenCentralIsDefault() {
        AetherArtifactProvider artifactResolver = artifactProviderFactory.create(configuration);

        List<RemoteRepository> remoteRepositories = artifactResolver.getRepositories();
        assertThat(remoteRepositories).hasSize(1);
        RemoteRepository mavenCentral = remoteRepositories.get(0);
        verify(mavenCentral, MAVEN_CENTRAL_ID, MAVEN_CENTRAL_URL);
    }

    @Test
    void mirrorOfMavenCentral() {
        Mirror mirror = mock(Mirror.class);
        doReturn(MAVEN_CENTRAL_ID).when(mirror)
            .mirrorOf();
        doReturn("https://mirror/central").when(mirror)
            .url();
        doReturn(Map.of("central-mirror", mirror)).when(repositories)
            .mirrors();

        AetherArtifactProvider artifactResolver = getArtifactResolver();
        List<RemoteRepository> remoteRepositories = artifactResolver.getRepositories();
        assertThat(remoteRepositories).hasSize(1);
        RemoteRepository mirrorRepository = remoteRepositories.get(0);
        verify(mirrorRepository, "central-mirror", "https://mirror/central");
        List<RemoteRepository> mirroredRepositories = mirrorRepository.getMirroredRepositories();
        assertThat(mirroredRepositories).hasSize(1);
        RemoteRepository mirroredRepository = mirroredRepositories.get(0);
        verify(mirroredRepository, MAVEN_CENTRAL_ID, MAVEN_CENTRAL_URL);
    }

    @Test
    void proxy() {
        String proxyHost = "proxy-host";
        int proxyPort = 80;
        String proxyUser = "foo";
        String proxyPassword = "bar";
        Proxy proxy = mock(Proxy.class);
        doReturn("https").when(proxy)
            .protocol();
        doReturn(proxyHost).when(proxy)
            .host();
        doReturn(proxyPort).when(proxy)
            .port();
        doReturn(of(proxyUser)).when(proxy).username();
        doReturn(of(proxyPassword)).when(proxy).password();
        doReturn(of(proxy)).when(configuration)
            .proxy();

        AetherArtifactProvider artifactResolver = getArtifactResolver();
        List<RemoteRepository> remoteRepositories = artifactResolver.getRepositories();
        assertThat(remoteRepositories).hasSize(1);
        RemoteRepository mavenCentral = remoteRepositories.get(0);
        org.eclipse.aether.repository.Proxy centralProxy = mavenCentral.getProxy();
        assertThat(centralProxy.getHost()).isEqualTo(proxyHost);
        assertThat(centralProxy.getPort()).isEqualTo(proxyPort);
        assertThat(centralProxy.getAuthentication()).isNotNull();
    }

    private AetherArtifactProvider getArtifactResolver() {
        ArtifactProvider artifactProvider = artifactProviderFactory.create(configuration);
        assertThat(artifactProvider).isInstanceOf(AetherArtifactProvider.class);
        return (AetherArtifactProvider) artifactProvider;
    }

    private static void verify(RemoteRepository repository, String expectedId, String expectedUrl) {
        assertThat(repository.getId()).isEqualTo(expectedId);
        assertThat(repository.getUrl()).isEqualTo(expectedUrl);
    }
}
