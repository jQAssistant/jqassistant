package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.VisitorHelper;
import org.objectweb.asm.ClassReader;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Implementation of the {@link AbstractFileScannerPlugin} for java classes.
 */
public class ClassScannerPlugin extends AbstractFileScannerPlugin<TypeDescriptor> {

    @Override
    protected void initialize() {
    }

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.endsWith(".class") && !file.startsWith("apple");
    }

    @Override
    public TypeDescriptor scanFile(StreamSource streamSource) throws IOException {
        Store store = getStore();
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(store);
        ClassVisitor visitor = new ClassVisitor(new VisitorHelper(store, resolverFactory));
        new ClassReader(streamSource.getInputStream()).accept(visitor, 0);
        return visitor.getTypeDescriptor();
    }

    @Override
    public TypeDescriptor scanDirectory(String name) throws IOException {
        return null;
    }
}
