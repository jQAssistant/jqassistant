package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.api.ArtifactScanner;
import com.buschmais.jqassistant.core.scanner.api.ClassScanner;
import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Implementation of the {@link ArtifactScanner}.
 */
public class ArtifactScannerImpl implements ArtifactScanner {

    private ClassScanner classScanner;
    private ScanListener scanListener;

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactScannerImpl.class);

    /**
     * Constructor.
     *
     * @param classScanner The {@link ClassScanner} instance.
     * @param scanListener The {@link ScanListener} instance.
     */
    public ArtifactScannerImpl(ClassScanner classScanner, ScanListener scanListener) {
        this.classScanner = classScanner;
        this.scanListener = scanListener;
    }

    /**
     * Constructor.
     *
     * @param classScanner The {@link ClassScanner} instance.
     */
    public ArtifactScannerImpl(ClassScanner classScanner) {
        this(classScanner, new ScanListener() {
        });
    }


    @Override
    public void scanArchive(File archive) throws IOException {
        scanArchive(null, archive);
    }

    @Override
    public void scanArchive(ArtifactDescriptor artifactDescriptor, File archive) throws IOException {
        if (!archive.exists()) {
            LOGGER.warn("Archive '{}' not found, skipping.", archive.getAbsolutePath());
        } else {
            LOGGER.info("Scanning archive '{}'.", archive.getAbsolutePath());
            long start = System.currentTimeMillis();
            ZipFile zipFile = new ZipFile(archive);
            int totalClasses = 0;
            int totalPackages = 0;
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
                    if (!e.isDirectory() && name.endsWith(".class")) {
                        String packageDirectory = name.substring(0, name.lastIndexOf('/'));
                        SortedSet<ZipEntry> packageEntries = entries.get(packageDirectory);
                        if (packageEntries == null) {
                            packageEntries = new TreeSet<>(zipEntryComparator);
                            entries.put(packageDirectory, packageEntries);
                            totalPackages++;
                        }
                        packageEntries.add(e);
                        totalClasses++;
                    }
                }
                int currentPackages = 0;
                int currentClasses = 0;
                LOGGER.info("Archive '{}' contains {} packages.", archive.getAbsolutePath(), entries.size());
                for (Map.Entry<String, SortedSet<ZipEntry>> e : entries.entrySet()) {
                    LOGGER.info("Scanning " + e.getKey() + " (" + currentPackages + "/" + totalPackages + " packages, " + currentClasses + "/" + totalClasses + " classes)");
                    scanListener.beforePackage();
                    try {
                        for (ZipEntry zipEntry : e.getValue()) {
                            TypeDescriptor typeDescriptor = scanInputStream(zipFile.getInputStream(zipEntry), zipEntry.getName());
                            artifactDescriptor.getContains().add(typeDescriptor);
                            currentClasses++;
                        }
                    } finally {
                        scanListener.afterPackage();
                    }
                    currentPackages++;
                }
            } finally {
                zipFile.close();
            }
            long end = System.currentTimeMillis();
            LOGGER.info("Scanned archive '{}' in {}ms.", archive.getAbsolutePath(), Long.valueOf(end - start));
        }
    }


    @Override
    public void scanClassDirectory(ArtifactDescriptor artifactDescriptor, File directory) throws IOException {
        final List<File> classFiles = new ArrayList<>();
        new DirectoryWalker<File>() {

            @Override
            protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                if (!file.isDirectory() && file.getName().endsWith(".class")) {
                    results.add(file);
                }
            }

            public void scan(File directory) throws IOException {
                super.walk(directory, classFiles);
            }
        }.scan(directory);
        if (classFiles.isEmpty()) {
            LOGGER.info("Directory '{}' does not contain class files, skipping.", directory.getAbsolutePath(), classFiles.size());
        } else {
            LOGGER.info("Scanning directory '{}' [{} class files].", directory.getAbsolutePath(), classFiles.size());
            URI directoryURI = directory.toURI();
            for (File classFile : classFiles) {
                TypeDescriptor typeDescriptor = scanInputStream(new FileInputStream(classFile), directoryURI.relativize(classFile.toURI()).toString());
                artifactDescriptor.getContains().add(typeDescriptor);
            }
        }
    }

    /**
     * Scan the given input stream.
     *
     * @param inputStream The input stream.
     * @param name        The name.
     * @return The type descriptor.
     * @throws IOException If scanning fails.
     */
    private TypeDescriptor scanInputStream(InputStream inputStream, String name) throws IOException {
        try {
            scanListener.beforeClass();
            return classScanner.scanInputStream(new BufferedInputStream(inputStream), name);
        } finally {
            scanListener.afterClass();
        }

    }
}
