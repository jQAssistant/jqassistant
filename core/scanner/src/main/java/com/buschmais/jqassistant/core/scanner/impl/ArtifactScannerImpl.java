package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.scanner.api.ArtifactScanner;
import com.buschmais.jqassistant.core.scanner.api.ArtifactScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.buschmais.jqassistant.core.scanner.api.ArtifactScannerPlugin.InputStreamSource;

/**
 * Implementation of the {@link ArtifactScanner}.
 */
public class ArtifactScannerImpl implements ArtifactScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactScannerImpl.class);

    private Store store;
    private Collection<ArtifactScannerPlugin> plugins;

    /**
     * Constructor.
     *
     * @param plugins The {@link ArtifactScannerPlugin}s to use for scanning.
     */
    public ArtifactScannerImpl(Store store, Collection<ArtifactScannerPlugin> plugins) {
        this.store = store;
        this.plugins = plugins;
    }

    @Override
    public void scanArchive(ArtifactDescriptor artifactDescriptor, File archive) throws IOException {
        if (!archive.exists()) {
            LOGGER.warn("Archive '{}' not found, skipping.", archive.getAbsolutePath());
        } else {
            LOGGER.info("Scanning archive '{}'.", archive.getAbsolutePath());
            long start = System.currentTimeMillis();
            final ZipFile zipFile = new ZipFile(archive);
            int totalFiles = 0;
            int totalDirectories = 0;
            try {
                final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
                Map<String, SortedSet<ZipEntry>> entries = new TreeMap<>();
                Comparator<ZipEntry> zipEntryComparator = new Comparator<ZipEntry>() {
                    @Override
                    public int compare(ZipEntry o1, ZipEntry o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                };
                while (zipEntries.hasMoreElements()) {
                    ZipEntry e = zipEntries.nextElement();
                    String name = e.getName();
                    String directory;
                    if (!e.isDirectory()) {
                        directory = name;
                    } else {
                        directory = name.substring(0, name.lastIndexOf('/'));
                    }
                    SortedSet<ZipEntry> directoryEntries = entries.get(directory);
                    if (directoryEntries == null) {
                        directoryEntries = new TreeSet<>(zipEntryComparator);
                        entries.put(directory, directoryEntries);
                        totalDirectories++;
                    }
                    directoryEntries.add(e);
                }
                int currentDirectories = 0;
                int currentFiles = 0;
                LOGGER.info("Archive '{}' contains {} directories.", archive.getAbsolutePath(), entries.size());
                for (Map.Entry<String, SortedSet<ZipEntry>> e : entries.entrySet()) {
                    LOGGER.info("Scanning " + e.getKey() + " (" + currentDirectories + "/" + totalDirectories + " directories, " + currentFiles + "/" + totalFiles + " files)");
                    for (final ZipEntry zipEntry : e.getValue()) {
                        String name = zipEntry.getName();
                        if (zipEntry.isDirectory()) {
                            scanDirectory(artifactDescriptor, name);
                        } else {
                            scanFile(artifactDescriptor, name, getStreamSource(zipFile.getInputStream(zipEntry)));
                        }
                        currentFiles++;
                    }
                    currentDirectories++;
                }
            } finally {
                zipFile.close();
            }
            long end = System.currentTimeMillis();
            LOGGER.info("Scanned archive '{}' in {}ms.", archive.getAbsolutePath(), Long.valueOf(end - start));
        }
    }

    @Override
    public void scanDirectory(ArtifactDescriptor artifactDescriptor, File directory) throws IOException {
        final List<File> files = new ArrayList<>();
        new DirectoryWalker<File>() {

            @Override
            protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
                results.add(directory);
                return true;
            }

            @Override
            protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                results.add(file);
            }

            public void scan(File directory) throws IOException {
                super.walk(directory, files);
            }
        }.scan(directory);
        if (files.isEmpty()) {
            LOGGER.info("Directory '{}' does not contain files, skipping.", directory.getAbsolutePath(), files.size());
        } else {
            LOGGER.info("Scanning directory '{}' [{} files].", directory.getAbsolutePath(), files.size());
            URI directoryURI = directory.toURI();
            for (final File file : files) {
                String name = directoryURI.relativize(file.toURI()).toString();
                if (file.isDirectory()) {
                    if (!StringUtils.isEmpty(name)) {
                        String directoryName = name.substring(0, name.length() - 1);
                        scanDirectory(artifactDescriptor, directoryName);
                    }
                } else {
                    scanFile(artifactDescriptor, name, getStreamSource(new FileInputStream(file)));
                }
            }
        }
    }

    @Override
    public void scanClasses(ArtifactDescriptor artifactDescriptor, Class<?>... classes) throws IOException {
        for (final Class<?> classType : classes) {
            final String resourceName = "/" + classType.getName().replace('.', '/') + ".class";
            scanFile(artifactDescriptor, resourceName, getStreamSource(classType.getResourceAsStream(resourceName)));
        }
    }

    @Override
    public void scanURLs(ArtifactDescriptor artifactDescriptor, URL... urls) throws IOException {
        for (final URL url : urls) {
            scanFile(artifactDescriptor, url.getPath() + "/" + url.getFile(), getStreamSource(url.openStream()));
        }
    }

    /**
     * Return a {@link InputStreamSource} for the given input stream.
     *
     * @param inputStream The input stream.
     * @return The {@link InputStreamSource}.
     */
    private InputStreamSource getStreamSource(final InputStream inputStream) {
        return new InputStreamSource() {
            @Override
            public InputStream openStream() throws IOException {
                return new BufferedInputStream((inputStream));
            }
        };
    }

    /**
     * Scans the given stream source                                          .
     *
     * @param artifactDescriptor The artifact descriptor containing the file.
     * @param name               The name of the file, relative to the artifact root directory.
     * @param streamSource       The stream source.
     * @throws IOException If scanning fails.
     */
    private void scanFile(ArtifactDescriptor artifactDescriptor, String name, InputStreamSource streamSource) throws IOException {
        for (ArtifactScannerPlugin plugin : this.plugins) {
            if (plugin.matches(name, false)) {
                LOGGER.info("Scanning directory '{}'", name);
                Descriptor descriptor = plugin.scanFile(store, streamSource);
                artifactDescriptor.getContains().add(descriptor);
            }
        }
    }

    /**
     * Scans the given stream source                                          .
     *
     * @param artifactDescriptor The artifact descriptor containing the file.
     * @param name               The name of the file, relative to the artifact root directory.
     * @throws IOException If scanning fails.
     */
    private void scanDirectory(ArtifactDescriptor artifactDescriptor, String name) throws IOException {
        for (ArtifactScannerPlugin plugin : this.plugins) {
            if (plugin.matches(name, true)) {
                LOGGER.info("Scanning directory '{}'", name);
                Descriptor descriptor = plugin.scanDirectory(store, name);
                artifactDescriptor.getContains().add(descriptor);
            }
        }
    }
}
