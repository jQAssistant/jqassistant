package com.buschmais.jqassistant.plugin.xml.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;

@Requires(FileDescriptor.class)
public class XmlFileScannerPlugin extends AbstractXmlFileScannerPlugin<XmlFileDescriptor> {

    public static final String PROPERTY_INCLUDE = "xml.file.include";
    public static final String PROPERTY_EXCLUDE = "xml.file.exclude";

    private FilePatternMatcher filePatternMatcher;

    @Override
    protected void configure() {
        filePatternMatcher = FilePatternMatcher.builder()
            .include(getStringProperty(PROPERTY_INCLUDE, "*.xml"))
            .exclude(getStringProperty(PROPERTY_EXCLUDE, null))
            .build();
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return filePatternMatcher.accepts(path);
    }

    @Override
    public XmlFileDescriptor scan(FileResource item, XmlFileDescriptor descriptor, String path, Scope scope, Scanner scanner) throws IOException {
        return descriptor;
    }

}
