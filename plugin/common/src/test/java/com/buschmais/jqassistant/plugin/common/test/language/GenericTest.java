package com.buschmais.jqassistant.plugin.common.test.language;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.Artifact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenericTest {

    @Test
    public void artifact() {
        assertThat(Artifact.getLanguage()).isEqualTo("Generic");
        ArtifactDescriptor artifactWithFqn = mock(ArtifactDescriptor.class);
        when(artifactWithFqn.getFullQualifiedName()).thenReturn("artifact");
        when(artifactWithFqn.getFileName()).thenReturn("artifact.jar");
        assertThat(Artifact.getSourceProvider()
            .getName(artifactWithFqn)).isEqualTo("artifact");
        ArtifactDescriptor artifactWithoutFqn = mock(ArtifactDescriptor.class);
        when(artifactWithoutFqn.getFileName()).thenReturn("artifact.jar");
        assertThat(Artifact.getSourceProvider()
            .getName(artifactWithFqn)).isEqualTo("artifact");
    }

}
