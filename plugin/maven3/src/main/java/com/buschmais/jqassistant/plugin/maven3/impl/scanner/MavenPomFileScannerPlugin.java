package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

/**
 * Scanner plugin for .pom files (as they can be found in M2 repositories).
 */
public class MavenPomFileScannerPlugin extends AbstractMavenPomScannerPlugin {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return !XmlScope.DOCUMENT.equals(scope) && path.toLowerCase().endsWith(".pom");
    }

}
