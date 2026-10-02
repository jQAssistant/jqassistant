package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.*;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GenericLanguageElementTest extends AbstractLanguageElementTest {

    public static final String SOURCE_FILENAME = "/test.txt";

    @Test
    void namedElement() {
        ArtifactFileDescriptor descriptor = mock(ArtifactFileDescriptor.class);
        doReturn("test").when(descriptor)
            .getName();
        doReturn(SOURCE_PATH_PREFIX + SOURCE_FILENAME).when(descriptor)
            .getPath();
        doReturn(SOURCE_FILENAME).when(descriptor)
            .getFileName();
        doReturn(newHashSet(getArtifactFileDescriptor())).when(descriptor)
            .getParents();

        verify(descriptor, Named, "test", SOURCE_FILENAME);
    }

    @Test
    void fileElement() {
        FileDescriptor descriptor = mock(FileDescriptor.class);
        doReturn(SOURCE_PATH_PREFIX + SOURCE_FILENAME).when(descriptor)
            .getPath();
        doReturn(SOURCE_FILENAME).when(descriptor)
            .getFileName();
        doReturn(Set.of(getArtifactFileDescriptor())).when(descriptor)
            .getParents();

        verify(descriptor, File, SOURCE_PATH_PREFIX + SOURCE_FILENAME, SOURCE_FILENAME);
    }

    @Test
    void artifactFileElement() {
        ArtifactFileDescriptor descriptor = mock(ArtifactFileDescriptor.class);
        doReturn(SOURCE_PATH_PREFIX + SOURCE_FILENAME).when(descriptor)
            .getPath();
        doReturn(SOURCE_FILENAME).when(descriptor)
            .getFileName();
        doReturn("group:name:type:version").when(descriptor)
            .getFullQualifiedName();
        doReturn(Set.of(getArtifactFileDescriptor())).when(descriptor)
            .getParents();

        verify(descriptor, Artifact, "group:name:type:version", SOURCE_FILENAME);
    }
}
