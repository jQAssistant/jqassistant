package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.GZipFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Scanner plugin for GZipped file resources.
 */
public class GZIPFileScannerPlugin extends AbstractResourceScannerPlugin<FileResource, FileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".gz");
    }

    @Override
    public FileDescriptor scan(final FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        try (FileResource fileResource = new AbstractFileResource() {

            @Override
            public InputStream createStream() throws IOException {
                return new GZIPInputStream(item.createStream());
            }
        }) {
            Store store = scanner.getContext().getStore();
            String uncompressedPath = path.substring(0, path.toLowerCase().indexOf(".gz"));
            FileDescriptor fileDescriptor = scanner.scan(fileResource, uncompressedPath, scope);
            GZipFileDescriptor archiveFileDescriptor;
            if (fileDescriptor != null) {
                 archiveFileDescriptor = store.addDescriptorType(fileDescriptor, GZipFileDescriptor.class);
            } else {
                archiveFileDescriptor =  store.create(GZipFileDescriptor.class);
            }
            return toFileDescriptor(fileResource, archiveFileDescriptor, path, scanner.getContext());
        }
    }
}
