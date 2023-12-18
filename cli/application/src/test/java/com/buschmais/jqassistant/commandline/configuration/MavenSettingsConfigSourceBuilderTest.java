package com.buschmais.jqassistant.commandline.configuration;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImpl;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class MavenSettingsConfigSourceBuilderTest {

    @Test
    void defaultMavenSettings() throws CliConfigurationException {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, empty());

        ConfigurationLoader<CliConfiguration> configurationLoader = new ConfigurationLoaderImpl<>(CliConfiguration.class);
        CliConfiguration configuration = configurationLoader.load(configSource);

        Optional<Proxy> proxyOptional = configuration.proxy();
        assertThat(proxyOptional).isPresent();
        Proxy proxy = proxyOptional.get();
        assertThat(proxy.protocol()).isEqualTo("https");
        assertThat(proxy.host()).isEqualTo("active-proxy-host");
        assertThat(proxy.port()).isEqualTo(3128);
        assertThat(proxy.username()).get()
            .isEqualTo("foo");
        assertThat(proxy.password()).get()
            .isEqualTo("bar");
        assertThat(proxy.nonProxyHosts()).hasValue("internal-host");

        Repositories repositories = configuration.repositories();
        assertThat(repositories.local()).isEqualTo(of(new File("~/local-repo")));

        Map<String, Mirror> mirrors = repositories.mirrors();
        assertThat(mirrors).hasSize(2);
        Mirror defaultMirror = mirrors.get("default");
        assertThat(defaultMirror).isNotNull();
        assertThat(defaultMirror.url()).isEqualTo("default-mirror-host");
        assertThat(defaultMirror.mirrorOf()).isEqualTo("*");
        Mirror otherMirror = mirrors.get("other");
        assertThat(otherMirror).isNotNull();
        assertThat(otherMirror.url()).isEqualTo("other-mirror-host");
        assertThat(otherMirror.mirrorOf()).isEqualTo("central");

        Map<String, Remote> remotes = repositories.remotes();
        assertThat(remotes).hasSize(2);

        Remote publicRemote = remotes.get("public");
        assertThat(publicRemote).isNotNull();
        assertThat(publicRemote.url()).isEqualTo("https://public-repo.acme.com/");

        Remote privateRemote = remotes.get("private");
        assertThat(privateRemote).isNotNull();
        assertThat(privateRemote.url()).isEqualTo("https://private-repo.acme.com/");
        assertThat(privateRemote.username()).isEqualTo(of("foo@bar.com"));
        assertThat(privateRemote.password()).isEqualTo(of("top-secret"));
        assertThat(configSource.getValue("custom")).isEqualTo("my-value");

    }

    @Test
    void customMavenSettings() throws CliConfigurationException {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());
        File customSettings = new File(userHome, "custom-maven-settings.xml");

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, Optional.of(customSettings));

        ConfigurationLoader<CliConfiguration> configurationLoader = new ConfigurationLoaderImpl<>(CliConfiguration.class);
        CliConfiguration configuration = configurationLoader.load(configSource);

        Repositories repositories = configuration.repositories();
        assertThat(repositories.local()).isEqualTo(of(new File("~/custom-repo")));
    }

    @Test
    void withoutMavenSettings() throws CliConfigurationException {
        File userHome = new File("invalid-userhome");

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, empty());

        assertThat(configSource.getPropertyNames())
            .isEmpty();
    }
}
