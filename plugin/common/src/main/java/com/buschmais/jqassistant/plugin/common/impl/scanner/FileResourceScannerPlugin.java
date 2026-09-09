package com.buschmais.jqassistant.plugin.common.impl.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

public class FileResourceScannerPlugin extends AbstractFileResourceScannerPlugin<FileResource, FileDescriptor> {

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<FileDescriptor> getDescriptorType() {
        return FileDescriptor.class;
    }

}
