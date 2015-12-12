package com.buschmais.jqassistant.plugin.cdi.impl.scanner;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.jcp.xmlns.xml.ns.javaee.Alternatives;
import org.jcp.xmlns.xml.ns.javaee.Beans;
import org.jcp.xmlns.xml.ns.javaee.Decorators;
import org.jcp.xmlns.xml.ns.javaee.Interceptors;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.cdi.api.model.BeansXmlDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;
import com.buschmais.jqassistant.plugin.xml.api.scanner.JAXBUnmarshaller;

@Requires(FileDescriptor.class)
public class BeansXmlScannerPlugin extends AbstractXmlFileScannerPlugin<BeansXmlDescriptor> {

    private JAXBUnmarshaller<Beans> unmarshaller;

    @Override
    public void initialize() {
        unmarshaller = new JAXBUnmarshaller<>(Beans.class);
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && ("/META-INF/beans.xml".equals(path) || "/WEB-INF/beans.xml".equals(path));
    }

    @Override
    public void scan(FileResource item, BeansXmlDescriptor beansXmlDescriptor, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Beans beans = unmarshaller.unmarshal(item);
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
    }

    private void addTypes(List<String> typeNames, List<TypeDescriptor> types, ScannerContext scannerContext) {
        for (String typeName : typeNames) {
            TypeDescriptor type = scannerContext.peek(TypeResolver.class).resolve(typeName, scannerContext).getTypeDescriptor();
            types.add(type);
        }
    }
}
