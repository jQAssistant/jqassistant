package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MavenArtifactHelperTest {

    @Test
    void baseVersion() {
        verifyBaseVersion("0.5.0", "0.5.0");
        verifyBaseVersion("0.5.0-SNAPSHOT", "0.5.0-SNAPSHOT");
        verifyBaseVersion("0.5.0-20141126.194537-53", "0.5.0-SNAPSHOT");
    }

    private void verifyBaseVersion(String version, String expected) {
        Coordinates coordinates = mock(Coordinates.class);
        doReturn(version).when(coordinates).getVersion();
        assertThat(MavenArtifactHelper.getBaseVersion(coordinates)).isEqualTo(expected);
    }

    @Test
    void snapshot() {
        verifySnapshot("0.5.0", false);
        verifySnapshot("0.5.0-SNAPSHOT", true);
        verifySnapshot("0.5.0-20141126.194537-53", true);
    }

    private void verifySnapshot(String version, boolean expected) {
        Coordinates coordinates = mock(Coordinates.class);
        doReturn(version).when(coordinates).getVersion();
        assertThat(MavenArtifactHelper.isSnapshot(coordinates)).isEqualTo(expected);
    }
}
