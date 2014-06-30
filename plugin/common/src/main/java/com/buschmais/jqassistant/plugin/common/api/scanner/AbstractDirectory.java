package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;

public abstract class AbstractDirectory {

    private File directory;

    private ArtifactDirectoryDescriptor descriptor;

    public AbstractDirectory(File directory, ArtifactDirectoryDescriptor descriptor) {
        this.directory = directory;
        this.descriptor = descriptor;
    }

    public File getDirectory() {
        return directory;
    }

    public ArtifactDirectoryDescriptor getDescriptor() {
        return descriptor;
    }
}
