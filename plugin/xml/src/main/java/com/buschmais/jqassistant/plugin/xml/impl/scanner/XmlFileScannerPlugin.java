package com.buschmais.jqassistant.plugin.xml.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

@Requires(FileDescriptor.class)
public class XmlFileScannerPlugin extends AbstractXmlFileScannerPlugin<XmlFileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return XmlScope.DOCUMENT.equals(scope);
    }

    @Override
    public void scan(FileResource item, XmlFileDescriptor descriptor, String path, Scope scope, Scanner scanner) throws IOException {
    }

}
