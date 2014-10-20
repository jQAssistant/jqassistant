package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.objectweb.asm.ClassReader;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java classes.
 */
public class ClassFileScannerPlugin extends AbstractScannerPlugin<FileResource, ClassFileDescriptor> {

    private static final byte[] CAFEBABE = new byte[] { -54, -2, -70, -66 };

    private DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory();

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        if (CLASSPATH.equals(scope) && path.endsWith(".class")) {
            // return true;
            try (InputStream stream = file.createStream()) {
                byte[] header = new byte[4];
                stream.read(header);
                return Arrays.equals(CAFEBABE, header);
            }
        }
        return false;
    }

    @Override
    public ClassFileDescriptor scan(FileResource file, String path, Scope scope, Scanner scanner) throws IOException {
        VisitorHelper visitorHelper = new VisitorHelper(scanner.getContext(), resolverFactory);
        ClassVisitor visitor = new ClassVisitor(visitorHelper);
        try (InputStream stream = file.createStream()) {
            new ClassReader(stream).accept(visitor, 0);
            return visitor.getTypeDescriptor();
        }
    }

}
