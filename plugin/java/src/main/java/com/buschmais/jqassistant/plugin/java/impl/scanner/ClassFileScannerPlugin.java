package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.objectweb.asm.ClassReader;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.StreamFactory;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java classes.
 */
public class ClassFileScannerPlugin extends AbstractScannerPlugin<StreamFactory> {

    private static final byte[] CAFEBABE = new byte[] { -54, -2, -70, -66 };
    
    DescriptorResolverFactory resolverFactory;
    
    private ThreadLocal<VisitorHelper> visitorHelper = new ThreadLocal<VisitorHelper>() {
        
        @Override
        protected VisitorHelper initialValue() {
            return new VisitorHelper(getStore(), resolverFactory);
        };
    };

    private ThreadLocal<ClassVisitor> visitor = new ThreadLocal<ClassVisitor>() {

        @Override
        protected ClassVisitor initialValue() {
            return new ClassVisitor(visitorHelper.get());
        }
        
    };
    
    @Override
    protected void initialize() {
        this.resolverFactory = new DescriptorResolverFactory(getStore());
    }

    @Override
    public Class<? super StreamFactory> getType() {
        return StreamFactory.class;
    }

    @Override
    public boolean accepts(StreamFactory streamFactory, String path, Scope scope) throws IOException {
        if (CLASSPATH.equals(scope) && path.endsWith(".class")) {
            try (InputStream stream = streamFactory.createStream()) {
                byte[] header = new byte[4];
                stream.read(header);
                return Arrays.equals(CAFEBABE, header);
            }
        }
        return false;
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(StreamFactory streamFactory, String path, Scope scope, Scanner scanner) throws IOException {
        try (InputStream stream = streamFactory.createStream()) {
            new ClassReader(stream).accept(this.visitor.get(), 0);
            ClassFileDescriptor classFileDescriptor = this.visitor.get().getTypeDescriptor();
            classFileDescriptor.setFileName(path);
            return asList(classFileDescriptor);
        }
    }

}
