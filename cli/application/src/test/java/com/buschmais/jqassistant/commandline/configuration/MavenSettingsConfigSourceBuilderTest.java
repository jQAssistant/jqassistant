package com.buschmais.jqassistant.commandline.configuration;

import java.io.File;
import java.net.URL;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImpl;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class MavenSettingsConfigSourceBuilderTest {

    @Test
    void mavenSettings() throws CliConfigurationException {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createConfigSource(userHome);

        ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();
        CliConfiguration configuration = configurationLoader.load(CliConfiguration.class, configSource);

        Repositories repositories = configuration.repositories();
        assertThat(repositories.local()).isEqualTo(of(new File("~/local-repo")));

        Map<String, Remote> remotes = repositories.remotes();
        assertThat(remotes).hasSize(2);

        Remote publicRemote = remotes
            .get("public");
        assertThat(publicRemote).isNotNull();
        assertThat(publicRemote.url()).isEqualTo("https://public-repo.acme.com/");

        Remote privateRemote = remotes
            .get("private");
        assertThat(privateRemote).isNotNull();
        assertThat(privateRemote.url()).isEqualTo("https://private-repo.acme.com/");
        assertThat(privateRemote.username()).isEqualTo(of("foo@bar.com"));
        assertThat(privateRemote.password()).isEqualTo(of("top-secret"));
        assertThat(configSource.getValue("custom")).isEqualTo("my-value");

    }

    @Test
    void withoutMavenSettings() throws CliConfigurationException {
        File userHome = new File("invalid-userhome");

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createConfigSource(userHome);

        configSource.getPropertyNames()
            .isEmpty();
    }
}
