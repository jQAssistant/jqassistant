package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectory;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;

public class ClassesDirectory extends AbstractDirectory {

    public ClassesDirectory(File directory, ArtifactDirectoryDescriptor descriptor) {
        super(directory, descriptor);
    }
}
