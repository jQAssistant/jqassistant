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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.DirectoryWalker;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.Store;

public class DependencyScanner {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DependencyScanner.class);

	private final Store store;

	public DependencyScanner(Store graphStore) {
		this.store = graphStore;
	}

	public void scanArchive(File archive) throws IOException {
		if (!archive.exists()) {
			LOGGER.warn("Archive '{}' not found, skipping.",
					archive.getAbsolutePath());
		} else {
			LOGGER.info("Scanning archive '{}'.",
					archive.getAbsolutePath());
			long start = System.currentTimeMillis();
			ZipFile zipFile = new ZipFile(archive);
			final Enumeration<? extends ZipEntry> zipEntries = zipFile
					.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String name = e.getName();
				if (name.endsWith(".class")) {
					scanInputStream(zipFile.getInputStream(e), name);
				}
			}
			long end = System.currentTimeMillis();
			LOGGER.info("Scanned archive '{}' in {}ms.",
					archive.getAbsolutePath(), Long.valueOf(end-start));
		}
	}

	public void scanDirectory(File directory) throws IOException {
		final List<File> classFiles = new ArrayList<File>();
		new DirectoryWalker<File>() {

			@Override
			protected void handleFile(File file, int depth,
					Collection<File> results) throws IOException {
				if (!file.isDirectory() && file.getName().endsWith(".class")) {
					results.add(file);
				}
			}

			public void scan(File directory) throws IOException {
				super.walk(directory, classFiles);
			}
		}.scan(directory);
		for (File classFile : classFiles) {
			scanFile(classFile);
		}
	}

	public void scanFile(File file) throws IOException {
		scanInputStream(new BufferedInputStream(new FileInputStream(file)),
				file.getName());
	}

	public void scanClass(Class<?> classType) throws IOException {
		String resourceName = "/" + classType.getName().replace('.', '/')
				+ ".class";
		scanInputStream(classType.getResourceAsStream(resourceName),
				resourceName);
	}

	public void scanInputStream(InputStream inputStream, String name)
			throws IOException {
		LOGGER.info("Scanning " + name);
		store.beginTransaction();
		ClassVisitor visitor = new ClassVisitor(store);
		new ClassReader(inputStream).accept(visitor, 0);
		store.endTransaction();
	}
}
