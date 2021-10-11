package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.source.ArtifactLocation;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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
        assertThat(sourceProvider.getName(descriptor), equalTo(expectedName));
        verifySourceLocation(descriptor, sourceProvider, expectedSourceFile, expectedStartLine, expectedEndLine);
    }

    private <D extends Descriptor> void verifySourceLocation(D descriptor, SourceProvider<D> sourceProvider, String expectedSourceFile,
            Optional<Integer> expectedStartLine, Optional<Integer> expectedEndLine) {
        Optional<FileLocation> optionalSourceLocation = sourceProvider.getSourceLocation(descriptor);
        assertThat(optionalSourceLocation.isPresent(), equalTo(true));
        FileLocation fileLocation = optionalSourceLocation.get();
        assertThat(fileLocation.getFileName(), equalTo(expectedSourceFile));
        assertThat(fileLocation.getStartLine(), equalTo(expectedStartLine));
        assertThat(fileLocation.getEndLine(), equalTo(expectedEndLine));
        verifyParentArtifact(fileLocation);
    }

    private void verifyParentArtifact(FileLocation sourceLocation) {
        Optional<ArtifactLocation> optionalParent = sourceLocation.getParent();
        assertThat(optionalParent.isPresent(), equalTo(true));
        ArtifactLocation artifactLocation = optionalParent.get();
        assertThat(artifactLocation.getFileName(), equalTo("test.jar"));
        assertThat(artifactLocation.getGroup(), equalTo(of("groupId")));
        assertThat(artifactLocation.getName(), equalTo(of("artifactId")));
        assertThat(artifactLocation.getType(), equalTo(of("jar")));
        assertThat(artifactLocation.getClassifier(), equalTo(of("jdk8")));
        assertThat(artifactLocation.getVersion(), equalTo(of("1.0.0")));
    }

}
