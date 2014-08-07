package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectory;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;

public class ClassesDirectory extends AbstractDirectory {

    private ArtifactDirectoryDescriptor directoryDescriptor;

    public ClassesDirectory(File directory) {
        this(directory, null);
    }

    public ClassesDirectory(File directory, ArtifactDirectoryDescriptor directoryDescriptor) {
        super(directory);
        this.directoryDescriptor = directoryDescriptor;
    }

    public ArtifactDirectoryDescriptor getDirectoryDescriptor() {
        return directoryDescriptor;
    }
}
