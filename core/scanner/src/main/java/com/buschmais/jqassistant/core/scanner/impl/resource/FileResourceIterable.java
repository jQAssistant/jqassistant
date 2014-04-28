package com.buschmais.jqassistant.core.scanner.impl.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;

/**
 * A resource iterable for processing a list of files.
 */
public class FileResourceIterable extends AbstractResourceIterable<File> {

    private final URI directoryURI;
    private final Iterator<File> iterator;

    public FileResourceIterable(Collection<FileScannerPlugin> plugins, File directory, List<File> files) {
        super(plugins);
        this.directoryURI = directory.toURI();
        this.iterator = files.iterator();
    }

    @Override
    protected boolean hasNextResource() {
        return iterator.hasNext();
    }

    @Override
    protected File nextResource() {
        return iterator.next();
    }

    @Override
    protected boolean isDirectory(File resource) {
        return resource.isDirectory();
    }

    @Override
    protected String getName(File resource) {
        String name = directoryURI.relativize(resource.toURI()).toString();
        if (resource.isDirectory()) {
            if (!StringUtils.isEmpty(name)) {
                return name.substring(0, name.length() - 1);
            }
            return name;
        } else {
            return name;
        }
    }

    @Override
    protected InputStream openInputStream(String fileName, File resource) throws IOException {
        return new FileInputStream(resource);
    }

    @Override
    protected void close() {
    }

}
