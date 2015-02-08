package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.impl.scanner.XmlFileScannerPlugin;

@Requires(XmlFileScannerPlugin.class)
public class MavenPomXmlScannerPlugin extends AbstractMavenPomScannerPlugin {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith("/pom.xml");
    }

    @Override
    protected MavenPomXmlDescriptor createDescriptor(Scanner scanner) {
        XmlDescriptor xmlDescriptor = scanner.getContext().peek(XmlDescriptor.class);
        return scanner.getContext().getStore().addDescriptorType(xmlDescriptor, MavenPomXmlDescriptor.class);
    }
}
