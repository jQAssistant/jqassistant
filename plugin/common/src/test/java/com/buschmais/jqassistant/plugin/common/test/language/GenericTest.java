package com.buschmais.jqassistant.plugin.common.test.language;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.ArtifactFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenericTest {

    @Test
    public void artifact() {
        assertThat(ArtifactFile.getLanguage(), equalTo("Generic"));
        ArtifactFileDescriptor artifactWithFqn = mock(ArtifactFileDescriptor.class);
        when(artifactWithFqn.getFullQualifiedName()).thenReturn("artifact");
        when(artifactWithFqn.getFileName()).thenReturn("artifact.jar");
        assertThat(ArtifactFile.getSourceProvider().getName(artifactWithFqn), equalTo("artifact"));
        ArtifactFileDescriptor artifactWithoutFqn = mock(ArtifactFileDescriptor.class);
        when(artifactWithoutFqn.getFileName()).thenReturn("artifact.jar");
        assertThat(ArtifactFile.getSourceProvider().getName(artifactWithFqn), equalTo("artifact"));
    }

}
