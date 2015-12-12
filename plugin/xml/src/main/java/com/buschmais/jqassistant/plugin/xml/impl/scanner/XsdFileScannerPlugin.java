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
public class XsdFileScannerPlugin extends AbstractXmlFileScannerPlugin<XmlFileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".xsd");
    }

    @Override
    public void scan(FileResource item, XmlFileDescriptor xmlFileDescriptor, String path, Scope scope, Scanner scanner) throws IOException {
    }
}
