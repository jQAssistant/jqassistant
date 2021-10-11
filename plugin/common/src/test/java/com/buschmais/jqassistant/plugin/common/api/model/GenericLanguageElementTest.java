package com.buschmais.jqassistant.plugin.common.api.model;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.ArtifactFile;
import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.File;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GenericLanguageElementTest extends AbstractLanguageElementTest {

    @Test
    void fileElement() {
        FileDescriptor descriptor = mock(FileDescriptor.class);
        doReturn("/test.txt").when(descriptor).getFileName();
        doReturn(newHashSet(getArtifactFileDescriptor())).when(descriptor).getParents();

        verify(descriptor, File, "/test.txt", "/test.txt");
    }

    @Test
    void artifactFileElement() {
        ArtifactFileDescriptor descriptor = mock(ArtifactFileDescriptor.class);
        doReturn("/test.txt").when(descriptor).getFileName();
        doReturn("group:name:type:version").when(descriptor).getFullQualifiedName();
        doReturn(newHashSet(getArtifactFileDescriptor())).when(descriptor).getParents();

        verify(descriptor, ArtifactFile, "group:name:type:version", "/test.txt");
    }
}
