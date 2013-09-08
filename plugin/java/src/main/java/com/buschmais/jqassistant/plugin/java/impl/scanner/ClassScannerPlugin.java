package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.VisitorHelper;
import org.objectweb.asm.ClassReader;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

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
    public TypeDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(store);
        ClassVisitor visitor = new ClassVisitor(new VisitorHelper(store, resolverFactory));
        new ClassReader(streamSource.getInputStream()).accept(visitor, 0);
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
