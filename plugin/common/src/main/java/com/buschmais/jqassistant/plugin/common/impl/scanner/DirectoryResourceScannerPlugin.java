package com.buschmais.jqassistant.plugin.common.impl.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.DirectoryResource;

public class DirectoryResourceScannerPlugin extends AbstractFileResourceScannerPlugin<DirectoryResource, DirectoryDescriptor> {

    @Override
    public Class<? extends DirectoryResource> getType() {
        return DirectoryResource.class;
    }

    @Override
    public Class<DirectoryDescriptor> getDescriptorType() {
        return DirectoryDescriptor.class;
    }
}
