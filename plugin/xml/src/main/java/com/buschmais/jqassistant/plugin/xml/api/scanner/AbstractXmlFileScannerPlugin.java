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

/**
 * Abstract base class for XML file scanners
 * 
 * @param <D>
 *            The descriptor type.
 * @param <P>
 *            The type of the actuall plugin.
 */
@Requires(FileDescriptor.class)
public abstract class AbstractXmlFileScannerPlugin<D extends XmlFileDescriptor, P extends ScannerPlugin<FileResource, D>> extends
        AbstractScannerPlugin<FileResource, D, P> {

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<D> getDescriptorType() {
        return getTypeParameter(AbstractXmlFileScannerPlugin.class, 0);
    }

    @Override
    public  D scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        FileDescriptor fileDescriptor = scanner.getContext().peek(FileDescriptor.class);
        Class<D> descriptorType = getDescriptorType();
        D xmlFileDescriptor = scanner.getContext().getStore().addDescriptorType(fileDescriptor, descriptorType);
        scanner.getContext().push(XmlDocumentDescriptor.class, xmlFileDescriptor);
        try (InputStream stream = item.createStream()) {
            scanner.scan(new StreamSource(stream), path, scope);
        } finally {
            scanner.getContext().pop(XmlDocumentDescriptor.class);
        }
        return scan(item, xmlFileDescriptor, path, scope, scanner);
    }

    public abstract D scan(FileResource item, D descriptor, String path, Scope scope, Scanner scanner) throws IOException;
}
