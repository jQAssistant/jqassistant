package com.buschmais.jqassistant.core.runtime.api.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider.getVersionProvider;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class VersionProviderTest {

    @Test
    void testGetVersion() {
        String version = getVersionProvider().getVersion();
        assertThat(version).isNotNull();
        assertThat(version.startsWith("$")).isFalse();
    }

    @Test
    void testGetMinorMajorVersion() {
        String versionMinor = getVersionProvider().getMinorVersion();
        assertThat(versionMinor).isNotNull();
        assertThat(versionMinor.startsWith("$")).isFalse();

        String versionMajor = getVersionProvider().getMajorVersion();
        assertThat(versionMajor).isNotNull();
        assertThat(versionMajor .startsWith("$")).isFalse();
    }
}
