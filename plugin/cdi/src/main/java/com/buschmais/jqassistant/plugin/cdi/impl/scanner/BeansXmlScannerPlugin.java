package com.buschmais.jqassistant.plugin.cdi.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jcp.xmlns.xml.ns.javaee.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.cdi.api.model.BeansXmlDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.impl.scanner.XmlFileScannerPlugin;

@Requires(XmlFileScannerPlugin.class)
public class BeansXmlScannerPlugin extends AbstractScannerPlugin<FileResource, BeansXmlDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeansXmlScannerPlugin.class);

    private static final JAXBContext jaxbContext;

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
    public BeansXmlDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        XmlDescriptor xmlDescriptor = context.peek(XmlDescriptor.class);
        BeansXmlDescriptor beansXmlDescriptor = context.getStore().addDescriptorType(xmlDescriptor, BeansXmlDescriptor.class);
        try (InputStream stream = item.createStream()) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Beans beans = unmarshaller.unmarshal(new StreamSource(stream), Beans.class).getValue();
            beansXmlDescriptor.setVersion(beans.getVersion());
            beansXmlDescriptor.setBeanDiscoveryMode(beans.getBeanDiscoveryMode());
            for (Object o : beans.getInterceptorsOrDecoratorsOrAlternatives()) {
                if (o instanceof Interceptors) {
                    addTypes(((Interceptors) o).getClazz(), beansXmlDescriptor.getInterceptors(), context);
                } else if (o instanceof Decorators) {
                    addTypes(((Decorators) o).getClazz(), beansXmlDescriptor.getDecorators(), context);
                } else if (o instanceof Alternatives) {
                    List<JAXBElement<String>> clazzOrStereotype = ((Alternatives) o).getClazzOrStereotype();
                    for (JAXBElement<String> element : clazzOrStereotype) {
                        TypeDescriptor alternative = scanner.getContext().peek(TypeResolver.class).resolve(element.getValue(), context).getTypeDescriptor();
                        beansXmlDescriptor.getAlternatives().add(alternative);
                    }
                }
            }
        } catch (JAXBException e) {
            LOGGER.warn("Cannot read CDI beans descriptor '{}'.", path, e);
        }
        return beansXmlDescriptor;
    }

    private void addTypes(List<String> typeNames, List<TypeDescriptor> types, ScannerContext scannerContext) {
        for (String typeName : typeNames) {
            TypeDescriptor type = scannerContext.peek(TypeResolver.class).resolve(typeName, scannerContext).getTypeDescriptor();
            types.add(type);
        }
    }
}
