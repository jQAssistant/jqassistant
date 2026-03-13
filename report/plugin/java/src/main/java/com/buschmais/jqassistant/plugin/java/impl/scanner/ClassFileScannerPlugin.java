package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java classes.
 */
@Requires(FileDescriptor.class)
public class ClassFileScannerPlugin extends AbstractScannerPlugin<FileResource, ClassFileDescriptor> {

    public static final byte[] CAFEBABE = new byte[] { -54, -2, -70, -66 };

    public static final String PROPERTY_INCLUDE_LOCAL_VARIABLES = "java.include.local-variables";

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassFileScannerPlugin.class);

    private ClassFileScannerConfiguration configuration;

    @Override
    protected void configure() {
        configuration = ClassFileScannerConfiguration.builder()
            .includeLocalVariables(getBooleanProperty(PROPERTY_INCLUDE_LOCAL_VARIABLES, false))
            .build();
    }

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
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
        ScannerContext context = scanner.getContext();
        final FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        ClassFileDescriptor classFileDescriptor = context.getStore()
            .addDescriptorType(fileDescriptor, ClassFileDescriptor.class);
        VisitorHelper visitorHelper = new VisitorHelper(context, configuration);
        final ClassVisitor visitor = new ClassVisitor(fileDescriptor, visitorHelper);
        try (InputStream inputStream = file.createStream()) {
            new ClassReader(inputStream).accept(visitor, 0);
            classFileDescriptor.setValid(true);
        } catch (RuntimeException e) {
            LOGGER.warn("Cannot scan class '" + path + "'.", e);
            classFileDescriptor.setValid(false);
        }
        return classFileDescriptor;
    }
}
