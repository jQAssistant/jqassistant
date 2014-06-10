package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.VisitorHelper;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java classes.
 */
public class ClassScannerPlugin extends AbstractScannerPlugin<InputStream> {

    private VisitorHelper visitorHelper;

    @Override
    protected void initialize() {
        DescriptorResolverFactory resolverFactory = new DescriptorResolverFactory(getStore());
        this.visitorHelper = new VisitorHelper(getStore(), resolverFactory);
    }

    @Override
    public Class<? super InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public boolean accepts(InputStream item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope) && path.endsWith(".class") && !path.contains("apple");
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(InputStream item, String path, Scope scope, Scanner scanner) throws IOException {
        ClassVisitor visitor = new ClassVisitor(visitorHelper);
        new ClassReader(item).accept(visitor, 0);
        TypeDescriptor typeDescriptor = visitor.getTypeDescriptor();
        return asList(typeDescriptor);
    }

}
