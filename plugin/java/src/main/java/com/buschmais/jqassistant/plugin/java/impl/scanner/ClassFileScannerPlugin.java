package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.MD5DigestDelegate;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java classes.
 */
@Requires(FileDescriptor.class)
public class ClassFileScannerPlugin extends AbstractScannerPlugin<FileResource, ClassFileDescriptor, ClassFileScannerPlugin> {

    public static final byte[] CAFEBABE = new byte[] { -54, -2, -70, -66 };

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassFileScannerPlugin.class);

    @Override
    protected ClassFileScannerPlugin getThis() {
        return this;
    }

    @Override
    protected boolean doAccepts(FileResource file, String path, Scope scope) throws IOException {
        if (CLASSPATH.equals(scope) && path.endsWith(".class")) {
            try (InputStream stream = file.createStream()) {
                byte[] header = new byte[CAFEBABE.length];
                int read = stream.read(header);
                return read == CAFEBABE.length && Arrays.equals(CAFEBABE, header);
            }
        }
        return false;
    }

    @Override
    public ClassFileDescriptor scan(FileResource file, String path, Scope scope, final Scanner scanner) throws IOException {
        final FileDescriptor fileDescriptor = scanner.getContext().peek(FileDescriptor.class);
        VisitorHelper visitorHelper = new VisitorHelper(scanner.getContext());
        final ClassVisitor visitor = new ClassVisitor(fileDescriptor, visitorHelper);
        ClassFileDescriptor classFileDescriptor;
        try (InputStream stream = file.createStream()) {
            classFileDescriptor = MD5DigestDelegate.getInstance().digest(stream, new MD5DigestDelegate.DigestOperation<ClassFileDescriptor>() {
                @Override
                public ClassFileDescriptor execute(InputStream inputStream) throws IOException {
                    new ClassReader(inputStream).accept(visitor, 0);
                    return visitor.getTypeDescriptor();
                }
            });
            classFileDescriptor.setValid(true);
        } catch (RuntimeException e) {
            LOGGER.warn("Cannot scan class '" + path + "'.", e);
            classFileDescriptor = visitor.getTypeDescriptor();
            if (classFileDescriptor == null) {
                classFileDescriptor = scanner.getContext().getStore().addDescriptorType(fileDescriptor, ClassFileDescriptor.class);
            }
            classFileDescriptor.setValid(false);
        }
        return classFileDescriptor;
    }
}
