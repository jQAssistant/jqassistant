package com.buschmais.jqassistant.commandline.plugin;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.configuration.Mirror;
import com.buschmais.jqassistant.commandline.configuration.Repositories;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;

import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.commandline.plugin.PluginResolverFactory.MAVEN_CENTRAL_ID;
import static com.buschmais.jqassistant.commandline.plugin.PluginResolverFactory.MAVEN_CENTRAL_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PluginResolverFactoryTest {

    private PluginResolverFactory pluginResolverFactory;

    @Mock
    private CliConfiguration configuration;

    @Mock
    private Repositories repositories;

    @BeforeEach
    void setUp() {
        doReturn(repositories).when(configuration)
            .repositories();
        URL userHomeUrl = PluginResolverFactoryTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());
        this.pluginResolverFactory = new PluginResolverFactory(userHome);
    }

    @Test
    void mavenCentralIsDefault() {
        PluginResolver pluginResolver = pluginResolverFactory.create(configuration);

        List<RemoteRepository> remoteRepositories = pluginResolver.getRepositories();
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

        PluginResolver pluginResolver = pluginResolverFactory.create(configuration);
        List<RemoteRepository> remoteRepositories = pluginResolver.getRepositories();
        assertThat(remoteRepositories).hasSize(1);
        RemoteRepository mirrorRepository = remoteRepositories.get(0);
        verify(mirrorRepository, "central-mirror", "https://mirror/central");
        List<RemoteRepository> mirroredRepositories = mirrorRepository.getMirroredRepositories();
        assertThat(mirroredRepositories).hasSize(1);
        RemoteRepository mirroredRepository = mirroredRepositories.get(0);
        verify(mirroredRepository, MAVEN_CENTRAL_ID, MAVEN_CENTRAL_URL);
    }
    
    private static void verify(RemoteRepository repository, String expectedId, String expectedUrl) {
        assertThat(repository.getId()).isEqualTo(expectedId);
        assertThat(repository.getUrl()).isEqualTo(expectedUrl);
    }
}
