package com.buschmais.jqassistant.core.scanner.impl.resource;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A resource iterable for processing a ZIP file.
 */
public class ZipResourceIterable extends AbstractResourceIterable<ZipEntry> {
    private final Enumeration<? extends ZipEntry> zipEntries;
    private final ZipFile zipFile;

    public ZipResourceIterable(Collection<FileScannerPlugin> plugins, ZipFile zipFile) {
        super(plugins);
        this.zipEntries = zipFile.entries();
        this.zipFile = zipFile;
    }

    @Override
    protected boolean hasNextResource() {
        return zipEntries.hasMoreElements();
    }

    @Override
    protected ZipEntry nextResource() {
        return zipEntries.nextElement();
    }

    @Override
    protected boolean isDirectory(ZipEntry element) {
        return element.isDirectory();
    }

    @Override
    protected String getName(ZipEntry element) {
        return element.getName();
    }

    @Override
    protected InputStream openInputStream(String fileName, ZipEntry element) throws IOException {
        return zipFile.getInputStream(element);
    }

    @Override
    protected void close() throws IOException {
        zipFile.close();
    }
}
