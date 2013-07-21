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
package com.buschmais.jqassistant.scanner;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.scanner.visitor.ClassVisitor;
import com.buschmais.jqassistant.store.api.Store;
import org.apache.commons.io.DirectoryWalker;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassScanner {

    public abstract static class ScanListener {

        public void beforePackage() {
        }

        public void afterPackage() {
        }

        public void beforeClass() {
        }

        public void afterClass() {
        }
    }

    /**
     * Defines the number of classes to be scanned before the store is flushed.
     */
    public static final int FLUSH_THRESHOLD = 50;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScanner.class);

    private final Store store;

    private final ScanListener scanListener;

    public ClassScanner(Store graphStore, ScanListener listener) {
        this.store = graphStore;
        this.scanListener = listener;
    }

    public ClassScanner(Store graphStore) {
        this.store = graphStore;
        this.scanListener = new ScanListener() {
        };
    }

    public void scanArchive(File archive) throws IOException {
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
                            scanInputStream(zipFile.getInputStream(zipEntry), zipEntry.getName());
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

    public void scanDirectory(File directory) throws IOException {
        final List<File> classFiles = new ArrayList<File>();
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
        int classCount = 0;
        for (File classFile : classFiles) {
            scanFile(classFile);
        }
    }

    public void scanFile(File file) throws IOException {
        scanInputStream(new BufferedInputStream(new FileInputStream(file)), file.getName());
    }

    public void scanClasses(Class<?>... classTypes) throws IOException {
        for (Class<?> classType : classTypes) {
            String resourceName = "/" + classType.getName().replace('.', '/') + ".class";
            scanInputStream(classType.getResourceAsStream(resourceName), resourceName);
        }
    }

    public void scanInputStream(InputStream inputStream, String name) throws IOException {
        LOGGER.info("Scanning " + name);
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(store);
        scanListener.beforeClass();
        try {
            ClassVisitor visitor = new ClassVisitor(resolverFactory);
            new ClassReader(inputStream).accept(visitor, 0);
        } finally {
            scanListener.afterClass();
        }
    }
}
