package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;

/**
 * Scanner plugin for .pom files (as they can be found in M2 repositories).
 */
public class MavenPomFileScannerPlugin extends AbstractMavenPomScannerPlugin {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".pom");
    }

    @Override
    protected MavenPomXmlDescriptor createDescriptor(FileResource item, String path, Scanner scanner) {
        return scanner.getContext().getStore().create(MavenPomXmlDescriptor.class);
    }

}
