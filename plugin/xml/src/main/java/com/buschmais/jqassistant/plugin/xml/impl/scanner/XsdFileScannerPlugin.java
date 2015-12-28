package com.buschmais.jqassistant.plugin.xml.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;

/**
 * Implementation of a scanner for XSD files containing XML schema definitions.
 */
public class XsdFileScannerPlugin extends AbstractXmlFileScannerPlugin<XmlFileDescriptor, XsdFileScannerPlugin> {

    @Override
    protected XsdFileScannerPlugin getThis() {
        return this;
    }

    @Override
    protected boolean doAccepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".xsd");
    }

    @Override
    public XmlFileDescriptor scan(FileResource item, XmlFileDescriptor xmlFileDescriptor, String path, Scope scope, Scanner scanner) throws IOException {
    return xmlFileDescriptor;
    }
}
