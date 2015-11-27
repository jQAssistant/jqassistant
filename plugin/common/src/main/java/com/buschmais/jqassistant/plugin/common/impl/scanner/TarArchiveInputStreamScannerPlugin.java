package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.TarArchiveDescriptor;

public class TarArchiveInputStreamScannerPlugin extends AbstractArchiveInputStreamScannerPlugin<TarArchiveInputStream, TarArchiveEntry, TarArchiveDescriptor> {

    @Override
    public Class<? extends TarArchiveInputStream> getType() {
        return TarArchiveInputStream.class;
    }

    @Override
    public Class<TarArchiveDescriptor> getDescriptorType() {
        return TarArchiveDescriptor.class;
    }

    @Override
    protected TarArchiveDescriptor getContainerDescriptor(TarArchiveInputStream item, ScannerContext scannerContext) {
        return scannerContext.getStore().create(TarArchiveDescriptor.class);
    }

    @Override
    protected TarArchiveEntry getNextEntry(TarArchiveInputStream container) throws IOException {
        return container.getNextTarEntry();
    }
}