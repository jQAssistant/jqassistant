package com.buschmais.jqassistant.core.resolver.api;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.resolver.configuration.*;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class MavenSettingsConfigSourceBuilderTest {

    @ConfigMapping(prefix = ArtifactResolverConfiguration.PREFIX)
    interface MavenConfiguration extends ArtifactResolverConfiguration {

        @WithDefault("false")
        boolean skip();

    }

    @Test
    void defaultMavenSettings() {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, empty(), emptyList());

        MavenConfiguration configuration = ConfigurationMappingLoader.builder(MavenConfiguration.class)
            .load(configSource);

        Optional<Proxy> proxyOptional = configuration.proxy();
        assertThat(proxyOptional).isPresent();
        Proxy proxy = proxyOptional.get();
        assertThat(proxy.protocol()).isPresent()
            .hasValue("https");
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
        assertThat(defaultMirror.mirrorOf()).isEqualTo("central");
        assertThat(defaultMirror.username()).isNotPresent();
        assertThat(defaultMirror.password()).isNotPresent();
        Mirror privateMirror = mirrors.get("private-mirror");
        assertThat(privateMirror).isNotNull();
        assertThat(privateMirror.url()).isEqualTo("private-mirror-host");
        assertThat(privateMirror.mirrorOf()).isEqualTo("*");
        assertThat(privateMirror.username()).isEqualTo(of("mirror-foo@bar.com"));
        assertThat(privateMirror.password()).isEqualTo(of("mirror-top-secret"));

        Map<String, Remote> remotes = repositories.remotes();
        assertThat(remotes).hasSize(2);

        Remote publicRemote = remotes.get("public");
        assertThat(publicRemote).isNotNull();
        assertThat(publicRemote.url()).isEqualTo("https://public-repo.acme.com/");
        Policy releasesPolicy = publicRemote.releases();
        assertThat(releasesPolicy.enabled()).isTrue();
        assertThat(releasesPolicy.updatePolicy()).isEqualTo("never");
        assertThat(releasesPolicy.checksumPolicy()).isEqualTo("ignore");
        Policy snapshotsPolicy = publicRemote.snapshots();
        assertThat(snapshotsPolicy.enabled()).isFalse();
        assertThat(snapshotsPolicy.updatePolicy()).isEqualTo("always");
        assertThat(snapshotsPolicy.checksumPolicy()).isEqualTo("fail");

        Remote privateRemote = remotes.get("private-repo");
        assertThat(privateRemote).isNotNull();
        assertThat(privateRemote.url()).isEqualTo("https://private-repo.acme.com/");
        assertThat(privateRemote.username()).isEqualTo(of("repo-foo@bar.com"));
        assertThat(privateRemote.password()).isEqualTo(of("repo-top-secret"));
        assertThat(configSource.getValue("custom")).isEqualTo("my-value");
    }

    @Test
    void userProfile() {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, empty(), List.of("user-profile"));

        MavenConfiguration configuration = ConfigurationMappingLoader.builder(MavenConfiguration.class)
            .load(configSource);

        assertThat(configuration.skip()).isTrue();
    }

    @Test
    void customMavenSettings() {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());
        File customSettings = new File(userHome, "custom-maven-settings.xml");

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, of(customSettings), emptyList());

        MavenConfiguration configuration = ConfigurationMappingLoader.builder(MavenConfiguration.class)
            .load(configSource);

        Repositories repositories = configuration.repositories();
        assertThat(repositories.local()).isEqualTo(of(new File("~/custom-repo")));
        assertThat(configuration.proxy()).isNotPresent();
    }

    @Test
    void customMavenSettingsWithoutLocalRepo() {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());
        File customSettings = new File(userHome, "custom-maven-settings-without-local-repo.xml");
        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, of(customSettings), emptyList());

        MavenConfiguration configuration = ConfigurationMappingLoader.builder(MavenConfiguration.class)
            .load(configSource);

        Repositories repositories = configuration.repositories();
        assertThat(repositories.local()).isEqualTo(of(new File(userHome, ".m2/repository")));
    }

    @Test
    void withoutMavenSettings() {
        File userHome = new File("invalid-userhome");

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource(userHome, empty(), emptyList());

        assertThat(configSource.getPropertyNames()).isEmpty();
    }
}
