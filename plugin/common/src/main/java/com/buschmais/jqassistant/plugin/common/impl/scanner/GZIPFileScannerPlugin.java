package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.GZipFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Scanner plugin for GZipped file resources.
 */
@Requires(FileDescriptor.class)
public class GZIPFileScannerPlugin
        extends AbstractScannerPlugin<FileResource, GZipFileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".gz");
    }

    @Override
    public GZipFileDescriptor scan(final FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        final FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        GZipFileDescriptor gZipFileDescriptor = store.addDescriptorType(fileDescriptor, GZipFileDescriptor.class);
        String uncompressedPath = path.substring(0, path.toLowerCase().indexOf(".gz"));
        try (FileResource fileResource = new BufferedFileResource(new AbstractVirtualFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return new GZIPInputStream(item.createStream());
            }

            @Override
            protected String getRelativePath() {
                return uncompressedPath;
            }
        })) {
            FileDescriptor entryDescriptor = scanner.scan(fileResource, uncompressedPath, scope);
            gZipFileDescriptor.getContains().add(entryDescriptor);
        }
        return gZipFileDescriptor;
    }
}
