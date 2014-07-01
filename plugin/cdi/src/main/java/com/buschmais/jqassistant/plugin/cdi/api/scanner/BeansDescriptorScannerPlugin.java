package com.buschmais.jqassistant.plugin.cdi.api.scanner;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jcp.xmlns.xml.ns.javaee.ObjectFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.cdi.api.type.BeansDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;

public class BeansDescriptorScannerPlugin extends AbstractScannerPlugin<InputStream> {

    private JAXBContext jaxbContext;

    private DescriptorResolverFactory descriptorResolverFactory;

    @Override
    protected void initialize() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context.", e);
        }
        descriptorResolverFactory = new DescriptorResolverFactory(getStore());
    }

    @Override
    public Class<? super InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public boolean accepts(InputStream item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && "/META-INF/beans.xml".equals(path) || "/WEB-INF/beans.xml".equals(path);
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(InputStream item, String path, Scope scope, Scanner scanner) throws IOException {
        BeansDescriptor beansDescriptor = getStore().create(BeansDescriptor.class);
        beansDescriptor.setFileName(path);
        return asList(beansDescriptor);
    }
}
