package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.resource.ClassResourceIterable;
import com.buschmais.jqassistant.core.scanner.impl.resource.FileResourceIterable;
import com.buschmais.jqassistant.core.scanner.impl.resource.UrlResourceIterable;
import com.buschmais.jqassistant.core.scanner.impl.resource.ZipResourceIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Implementation of the {@link FileScanner}.
 */
public class FileScannerImpl implements FileScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerImpl.class);

    private Collection<FileScannerPlugin> plugins;

    /**
     * Constructor.
     *
     * @param plugins The {@link FileScannerPlugin}s to use for scanning.
     */
    public FileScannerImpl(Collection<FileScannerPlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Iterable<Descriptor> scanArchive(File archive) throws IOException {
        if (!archive.exists()) {
            throw new IOException("Archive '" + archive.getAbsolutePath() + "' not found.");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scanning archive '{}'.", archive.getAbsolutePath());
        }
        final ZipFile zipFile = new ZipFile(archive);
        return new ZipResourceIterable(plugins, zipFile);
    }

    @Override
    public Iterable<Descriptor> scanDirectory(File directory) throws IOException {
        return scanDirectory(directory, true);
    }

    @Override
    public Iterable<Descriptor> scanDirectory(File directory, final boolean recursive) throws IOException {
        final List<File> files = new ArrayList<>();
        new DirectoryWalker<File>() {

            @Override
            protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
                if (recursive) {
                    results.add(directory);
                    return true;
                }
                return depth == 0;
            }

            @Override
            protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                results.add(file);
            }

            public void scan(File directory) throws IOException {
                super.walk(directory, files);
            }
        }.scan(directory);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scanning directory '{}' [{} files].", directory.getAbsolutePath(), files.size());
        }
        return scanFiles(directory, files);
    }

    @Override
    public Iterable<Descriptor> scanFiles(File directory, List<File> files) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scanning directory '{}' [{} files].", directory.getAbsolutePath(), files.size());
        }
        return new FileResourceIterable(plugins, directory, files);
    }

    @Override
    public Iterable<Descriptor> scanClasses(final Class<?>... classes) throws IOException {
        return new ClassResourceIterable(plugins, classes);
    }

    @Override
    public Iterable<Descriptor> scanURLs(final URL... urls) throws IOException {
        return new UrlResourceIterable(plugins, urls);
    }

}
