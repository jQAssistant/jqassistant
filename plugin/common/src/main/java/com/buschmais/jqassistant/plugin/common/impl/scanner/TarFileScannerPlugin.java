package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.TarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * Scanner plugin for TAR file resources.
 */
@Requires(FileDescriptor.class)
public class TarFileScannerPlugin extends AbstractScannerPlugin<FileResource, TarArchiveDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".tar");
    }

    @Override
    public TarArchiveDescriptor scan(final FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        return scanner.scan(new TarArchiveInputStream(item.createStream()), path, scope);
    }
}
