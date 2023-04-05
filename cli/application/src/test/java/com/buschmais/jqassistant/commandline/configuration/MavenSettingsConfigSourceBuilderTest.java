package com.buschmais.jqassistant.commandline.configuration;

import java.io.File;
import java.net.URL;

import com.buschmais.jqassistant.commandline.CliConfigurationException;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MavenSettingsConfigSourceBuilderTest {

    @Test
    void mavenSettings() throws CliConfigurationException {
        URL userHomeUrl = MavenSettingsConfigSourceBuilderTest.class.getResource("/userhome");
        File userHome = new File(userHomeUrl.getFile());

        ConfigSource configSource = MavenSettingsConfigSourceBuilder.createConfigSource(userHome);

        assertThat(configSource.getValue("jqassistant.repositories.local")).isEqualTo("~/local-repo");
        assertThat(configSource.getValue("jqassistant.repositories.remote[0].url")).isEqualTo("https://public-repo.acme.com/");
        assertThat(configSource.getValue("jqassistant.repositories.remote[1].url")).isEqualTo("https://private-repo.acme.com/");
        assertThat(configSource.getValue("jqassistant.repositories.remote[1].username")).isEqualTo("foo@bar.com");
        assertThat(configSource.getValue("jqassistant.repositories.remote[1].password")).isEqualTo("top-secret");
    }

}
