package com.buschmais.jqassistant.plugin.cdi.impl.scanner;

import static java.util.Arrays.asList;

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
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.cdi.api.type.BeansDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;

public class BeansDescriptorScannerPlugin extends AbstractScannerPlugin<InputStream> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeansDescriptorScannerPlugin.class);

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
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Beans beans = unmarshaller.unmarshal(new StreamSource(item), Beans.class).getValue();
            beansDescriptor.setVersion(beans.getVersion());
            beansDescriptor.setBeanDiscoveryMode(beans.getBeanDiscoveryMode());
            for (Object o : beans.getInterceptorsOrDecoratorsOrAlternatives()) {
                if (o instanceof Interceptors) {
                    addTypes(((Interceptors) o).getClazz(), beansDescriptor.getInterceptors());
                } else if (o instanceof Decorators) {
                    addTypes(((Decorators) o).getClazz(), beansDescriptor.getAlternatives());
                } else if (o instanceof Alternatives) {
                    List<JAXBElement<String>> clazzOrStereotype = ((Alternatives) o).getClazzOrStereotype();
                    for (JAXBElement<String> element : clazzOrStereotype) {
                        TypeDescriptor alternative = descriptorResolverFactory.getTypeDescriptorResolver().resolve(element.getValue());
                        beansDescriptor.getAlternatives().add(alternative);
                    }
                }
            }
        } catch (JAXBException e) {
            LOGGER.warn("Cannot read CDI beans descriptor '{}'.", path, e);
        }
        return asList(beansDescriptor);
    }

    private void addTypes(List<String> typeNames, List<TypeDescriptor> types) {
        for (String typeName : typeNames) {
            TypeDescriptor type = descriptorResolverFactory.getTypeDescriptorResolver().resolve(typeName);
            types.add(type);
        }
    }
}
