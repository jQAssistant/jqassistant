package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverProvider;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

public class FileResourceScannerPlugin extends AbstractScannerPlugin<FileResource, FileDescriptor> {
    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public FileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Descriptor descriptor = resolve(item, path, context);
        Store store = context.getStore();
        FileDescriptor fileDescriptor;
        if (descriptor != null) {
            fileDescriptor = store.addDescriptorType(descriptor, FileDescriptor.class);
        } else {
            fileDescriptor = store.create(FileDescriptor.class);
        }
        fileDescriptor.setFileName(path);
        return fileDescriptor;
    }

    /**
     * Resolves a descriptor from a given path using the
     * {@link FileResolverProvider} registered in the scanner context.
     * 
     * @param fileResource
     *            The file resource.
     * @param path
     *            The path.
     * @param context
     *            The context
     * @return The resolved descriptor or <code>null</code>.
     */
    private Descriptor resolve(FileResource fileResource, String path, ScannerContext context) {
        FileResolverProvider fileResolverProvider = context.peek(FileResolverProvider.class);
        if (fileResolverProvider != null) {
            for (FileResolver fileResolver : fileResolverProvider.get()) {
                Descriptor resolvedDescriptor = fileResolver.resolve(fileResource, path, context);
                if (resolvedDescriptor != null) {
                    return resolvedDescriptor;
                }
            }
        }
        return null;
    }
}
