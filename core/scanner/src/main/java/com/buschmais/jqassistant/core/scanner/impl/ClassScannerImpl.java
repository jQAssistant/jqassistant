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
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
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

    /**
     * Constructor.
     *
     * @param graphStore The store to use.
     */
    public ClassScannerImpl(Store graphStore) {
        this.store = graphStore;

    }

    @Override
    public Collection<TypeDescriptor> scanClasses(Class<?>... classTypes) throws IOException {
        List<TypeDescriptor> typeDescriptors = new ArrayList<>();
        for (Class<?> classType : classTypes) {
            String resourceName = "/" + classType.getName().replace('.', '/') + ".class";
            TypeDescriptor typeDescriptor = scanInputStream(classType.getResourceAsStream(resourceName), resourceName);
            typeDescriptors.add(typeDescriptor);
        }
        return typeDescriptors;
    }

    @Override
    public TypeDescriptor scanInputStream(InputStream inputStream, String name) throws IOException {
        LOGGER.info("Scanning " + name);
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(store);
        ClassVisitor visitor = new ClassVisitor(new VisitorHelper(store, resolverFactory));
        new ClassReader(inputStream).accept(visitor, 0);
        return visitor.getTypeDescriptor();
    }
}
