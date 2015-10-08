package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverStrategy;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationDescriptor;

/**
 * A file resolver strátegy for web applications.
 */
public class WebApplicationFileResolverStrategy implements FileResolverStrategy {

    @Override
    public Descriptor require(String path, ScannerContext context) {
        WebApplicationDescriptor webApplicationDescriptor = context.peek(WebApplicationDescriptor.class);
        FileDescriptor fileDescriptor = webApplicationDescriptor.find(path);
        if (fileDescriptor == null) {
            fileDescriptor = context.getStore().create(FileDescriptor.class);
            fileDescriptor.setFileName(path);
            webApplicationDescriptor.getRequires().add(fileDescriptor);
        }
        return fileDescriptor;
    }

    @Override
    public FileDescriptor match(String path, ScannerContext context) {
        WebApplicationDescriptor webApplicationDescriptor = context.peek(WebApplicationDescriptor.class);
        FileDescriptor fileDescriptor = webApplicationDescriptor.find(path);
        if (fileDescriptor != null) {
            webApplicationDescriptor.getRequires().remove(fileDescriptor);
        } else {
            fileDescriptor = context.getStore().create(FileDescriptor.class);
            fileDescriptor.setFileName(path);
        }
        return fileDescriptor;
    }
}
