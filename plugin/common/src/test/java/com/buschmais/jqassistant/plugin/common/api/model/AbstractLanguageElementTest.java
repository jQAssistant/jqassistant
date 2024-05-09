package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.source.ArtifactLocation;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

abstract public class AbstractLanguageElementTest {

    protected ArtifactFileDescriptor getArtifactFileDescriptor() {
        ArtifactFileDescriptor artifactFileDescriptor = mock(ArtifactFileDescriptor.class);
        doReturn("test.jar").when(artifactFileDescriptor).getFileName();
        doReturn("groupId").when(artifactFileDescriptor).getGroup();
        doReturn("artifactId").when(artifactFileDescriptor).getName();
        doReturn("jar").when(artifactFileDescriptor).getType();
        doReturn("jdk8").when(artifactFileDescriptor).getClassifier();
        doReturn("1.0.0").when(artifactFileDescriptor).getVersion();
        return artifactFileDescriptor;
    }

    protected <D extends Descriptor> void verify(D descriptor, LanguageElement languageElement, String expectedName, String expectedSourceFile) {
        verify(descriptor, languageElement, expectedName, expectedSourceFile, empty(), empty());
    }

    protected <D extends Descriptor> void verify(D descriptor, LanguageElement languageElement, String expectedName, String expectedSourceFile,
            Optional<Integer> expectedStartLine, Optional<Integer> expectedEndLine) {
        SourceProvider<D> sourceProvider = languageElement.getSourceProvider();
        assertThat(sourceProvider.getName(descriptor)).isEqualTo(expectedName);
        verifySourceLocation(descriptor, sourceProvider, expectedSourceFile, expectedStartLine, expectedEndLine);
    }

    private <D extends Descriptor> void verifySourceLocation(D descriptor, SourceProvider<D> sourceProvider, String expectedSourceFile,
            Optional<Integer> expectedStartLine, Optional<Integer> expectedEndLine) {
        Optional<FileLocation> optionalSourceLocation = sourceProvider.getSourceLocation(descriptor);
        assertThat(optionalSourceLocation.isPresent()).isEqualTo(true);
        FileLocation fileLocation = optionalSourceLocation.get();
        assertThat(fileLocation.getFileName()).isEqualTo(expectedSourceFile);
        assertThat(fileLocation.getStartLine()).isEqualTo(expectedStartLine);
        assertThat(fileLocation.getEndLine()).isEqualTo(expectedEndLine);
        verifyParentArtifact(fileLocation);
    }

    private void verifyParentArtifact(FileLocation sourceLocation) {
        Optional<ArtifactLocation> optionalParent = sourceLocation.getParent();
        assertThat(optionalParent.isPresent()).isEqualTo(true);
        ArtifactLocation artifactLocation = optionalParent.get();
        assertThat(artifactLocation.getFileName()).isEqualTo("test.jar");
        assertThat(artifactLocation.getGroup()).isEqualTo(of("groupId"));
        assertThat(artifactLocation.getName()).isEqualTo(of("artifactId"));
        assertThat(artifactLocation.getType()).isEqualTo(of("jar"));
        assertThat(artifactLocation.getClassifier()).isEqualTo(of("jdk8"));
        assertThat(artifactLocation.getVersion()).isEqualTo(of("1.0.0"));
    }

}
