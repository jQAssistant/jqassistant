/***
 * ASM examples: examples showing how ASM can be used
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.scanner.api.ClassScanner;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.core.scanner.impl.visitor.ClassVisitor;
import com.buschmais.jqassistant.core.scanner.impl.visitor.VisitorHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.commons.io.DirectoryWalker;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Implementation of the {@link ClassScanner}.
 */
public class ClassScannerImpl implements ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScannerImpl.class);

    private final Store store;

    private final ScanListener scanListener;

    /**
     * Constructor.
     *
     * @param graphStore The store to use.
     * @param listener   The listener to fire events to.
     */
    public ClassScannerImpl(Store graphStore, ScanListener listener) {
        this.store = graphStore;
        this.scanListener = listener;
    }

    public ClassScannerImpl(Store graphStore) {
        this(graphStore, new ScanListener() {
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
                Map<String, SortedSet<ZipEntry>> entries = new TreeMap<String, SortedSet<ZipEntry>>();
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
                            packageEntries = new TreeSet<ZipEntry>(zipEntryComparator);
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
                            scanInputStream(artifactDescriptor, zipFile.getInputStream(zipEntry), zipEntry.getName());
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
    public void scanClassDirectory(File directory) throws IOException {
        scanClassDirectory(null, directory);
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
                scanInputStream(artifactDescriptor, new BufferedInputStream(new FileInputStream(classFile)), directoryURI.relativize(classFile.toURI()).toString());
            }
        }
    }

    @Override
    public void scanClassFile(ArtifactDescriptor artifactDescriptor, File file) throws IOException {
        scanInputStream(artifactDescriptor, new BufferedInputStream(new FileInputStream(file)), file.getAbsolutePath());
    }

    @Override
    public void scanClasses(Class<?>... classTypes) throws IOException {
        this.scanClasses(null, classTypes);
    }

    @Override
    public void scanClasses(ArtifactDescriptor artifact, Class<?>... classTypes) throws IOException {
        for (Class<?> classType : classTypes) {
            String resourceName = "/" + classType.getName().replace('.', '/') + ".class";
            scanInputStream(artifact, classType.getResourceAsStream(resourceName), resourceName);
        }
    }

    @Override
    public void scanInputStream(ArtifactDescriptor artifactDescriptor, InputStream inputStream, String name) throws IOException {
        LOGGER.info("Scanning " + name);
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(store);
        scanListener.beforeClass();
        try {
            ClassVisitor visitor = new ClassVisitor(artifactDescriptor, new VisitorHelper(store, resolverFactory));
            new ClassReader(inputStream).accept(visitor, 0);
        } finally {
            scanListener.afterClass();
        }
    }
}
