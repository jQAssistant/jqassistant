package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.objectweb.asm.ClassReader;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.File;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java classes.
 */
public class ClassFileScannerPlugin extends AbstractScannerPlugin<File> {

    private static final byte[] CAFEBABE = new byte[] { -54, -2, -70, -66 };

    private ClassVisitor visitor;

    @Override
    protected void initialize() {
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(getStore());
        VisitorHelper visitorHelper = new VisitorHelper(getStore(), resolverFactory);
        visitor = new ClassVisitor(visitorHelper);
    }

    @Override
    public Class<? super File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File file, String path, Scope scope) throws IOException {
        if (CLASSPATH.equals(scope) && path.endsWith(".class")) {
            try (InputStream stream = file.createStream()) {
                byte[] header = new byte[4];
                stream.read(header);
                return Arrays.equals(CAFEBABE, header);
            }
        }
        return false;
    }

    @Override
    public FileDescriptor scan(File file, String path, Scope scope, Scanner scanner) throws IOException {
        try (InputStream stream = file.createStream()) {
            new ClassReader(stream).accept(visitor, 0);
            return visitor.getTypeDescriptor();
        }
    }

}
