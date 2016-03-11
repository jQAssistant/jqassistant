package com.buschmais.jqassistant.plugin.xml.api.scanner;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDocumentDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for XML file scanners
 *
 * @param <D> The descriptor type.
 */
@Requires(FileDescriptor.class)
public abstract class AbstractXmlFileScannerPlugin<D extends XmlFileDescriptor> extends
        AbstractScannerPlugin<FileResource, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXmlFileScannerPlugin.class);

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<D> getDescriptorType() {
        return getTypeParameter(AbstractXmlFileScannerPlugin.class, 0);
    }

    @Override
    public final D scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        FileDescriptor fileDescriptor = scanner.getContext().peek(FileDescriptor.class);
        Class<D> descriptorType = getDescriptorType();
        D xmlFileDescriptor = scanner.getContext().getStore().addDescriptorType(fileDescriptor, descriptorType);
        scanner.getContext().push(XmlDocumentDescriptor.class, xmlFileDescriptor);
        try (InputStream stream = item.createStream()) {
            scanner.scan(new StreamSource(stream), path, scope);
        } finally {
            scanner.getContext().pop(XmlDocumentDescriptor.class);
        }
        if (!xmlFileDescriptor.isXmlWellFormed()) {
            LOGGER.warn("XML content is not well-formed for item '{}', skipping.", path);
        } else {
            return scan(item, xmlFileDescriptor, path, scope, scanner);
        }
        return xmlFileDescriptor;
    }

    public abstract D scan(FileResource item, D descriptor, String path, Scope scope, Scanner scanner) throws IOException;
}
