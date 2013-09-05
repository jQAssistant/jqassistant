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

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.core.scanner.impl.visitor.ClassVisitor;
import com.buschmais.jqassistant.core.scanner.impl.visitor.VisitorHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import org.objectweb.asm.ClassReader;

import java.io.*;

/**
 * Implementation of the {@link com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin} for java classes.
 */
public class ClassScannerPlugin implements FileScannerPlugin<TypeDescriptor> {

    private int scannedClasses;

    /**
     * Constructor.
     */
    public ClassScannerPlugin() {
        this.scannedClasses = 0;

    }

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.endsWith(".class");
    }

    @Override
    public TypeDescriptor scanFile(Store store, InputStreamSource streamSource) throws IOException {
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(store);
        ClassVisitor visitor = new ClassVisitor(new VisitorHelper(store, resolverFactory));
        new ClassReader(streamSource.openStream()).accept(visitor, 0);
        TypeDescriptor typeDescriptor = visitor.getTypeDescriptor();
        scannedClasses++;
        return typeDescriptor;
    }

    @Override
    public TypeDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }

    /**
     * Return the number of classes scanned by this plugin.
     *
     * @return the number of classes scanned by this plugin.
     */
    public int getScannedClasses() {
        return scannedClasses;
    }
}
