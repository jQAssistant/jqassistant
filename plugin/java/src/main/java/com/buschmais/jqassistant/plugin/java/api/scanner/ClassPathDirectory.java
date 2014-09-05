package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectory;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;

public class ClassPathDirectory extends AbstractDirectory {

    private ArtifactDirectoryDescriptor directoryDescriptor;

    public ClassPathDirectory(File directory) {
        this(directory, null);
    }

    public ClassPathDirectory(File directory, ArtifactDirectoryDescriptor directoryDescriptor) {
        super(directory);
        this.directoryDescriptor = directoryDescriptor;
    }

    public ArtifactDirectoryDescriptor getDirectoryDescriptor() {
        return directoryDescriptor;
    }
}
