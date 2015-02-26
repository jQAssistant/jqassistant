package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

public class MavenPomXmlFileScannerPlugin extends AbstractMavenPomScannerPlugin {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith("/pom.xml");
    }

    @Override
    protected MavenPomXmlDescriptor createDescriptor(FileResource item, String path, Scanner scanner) {
        XmlFileDescriptor xmlFileDescriptor = scanner.scan(item, path, XmlScope.DOCUMENT);
        return scanner.getContext().getStore().addDescriptorType(xmlFileDescriptor, MavenPomXmlDescriptor.class);
    }
}
