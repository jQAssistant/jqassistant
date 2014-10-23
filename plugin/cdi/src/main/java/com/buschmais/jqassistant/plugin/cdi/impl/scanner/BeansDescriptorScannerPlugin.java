package com.buschmais.jqassistant.plugin.cdi.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jcp.xmlns.xml.ns.javaee.Alternatives;
import org.jcp.xmlns.xml.ns.javaee.Beans;
import org.jcp.xmlns.xml.ns.javaee.Decorators;
import org.jcp.xmlns.xml.ns.javaee.Interceptors;
import org.jcp.xmlns.xml.ns.javaee.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.cdi.api.model.BeansDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;

public class BeansDescriptorScannerPlugin extends AbstractScannerPlugin<FileResource, BeansDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeansDescriptorScannerPlugin.class);

    private static final JAXBContext jaxbContext;

    private DescriptorResolverFactory descriptorResolverFactory = new DescriptorResolverFactory();

    static {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && "/META-INF/beans.xml".equals(path) || "/WEB-INF/beans.xml".equals(path);
    }

    @Override
    public BeansDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        BeansDescriptor beansDescriptor = context.getStore().create(BeansDescriptor.class);
        try (InputStream stream = item.createStream()) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Beans beans = unmarshaller.unmarshal(new StreamSource(stream), Beans.class).getValue();
            beansDescriptor.setVersion(beans.getVersion());
            beansDescriptor.setBeanDiscoveryMode(beans.getBeanDiscoveryMode());
            for (Object o : beans.getInterceptorsOrDecoratorsOrAlternatives()) {
                if (o instanceof Interceptors) {
                    addTypes(((Interceptors) o).getClazz(), beansDescriptor.getInterceptors(), context);
                } else if (o instanceof Decorators) {
                    addTypes(((Decorators) o).getClazz(), beansDescriptor.getDecorators(), context);
                } else if (o instanceof Alternatives) {
                    List<JAXBElement<String>> clazzOrStereotype = ((Alternatives) o).getClazzOrStereotype();
                    for (JAXBElement<String> element : clazzOrStereotype) {
                        TypeDescriptor alternative = descriptorResolverFactory.getTypeDescriptorResolver().resolve(element.getValue(), context);
                        beansDescriptor.getAlternatives().add(alternative);
                    }
                }
            }
        } catch (JAXBException e) {
            LOGGER.warn("Cannot read CDI beans descriptor '{}'.", path, e);
        }
        return beansDescriptor;
    }

    private void addTypes(List<String> typeNames, List<TypeDescriptor> types, ScannerContext scannerContext) {
        for (String typeName : typeNames) {
            TypeDescriptor type = descriptorResolverFactory.getTypeDescriptorResolver().resolve(typeName, scannerContext);
            types.add(type);
        }
    }
}
