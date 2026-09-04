package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.LocalDescriptor;
import com.buschmais.xo.api.Query.Result;

/**
 * File resolver for the local file system using absolute paths.
 * <p>
 */
public class LocalFileSystemFileResolver extends AbstractFileResolver {

    public LocalFileSystemFileResolver() {
        super("", LocalFileSystemFileResolver.class.getName());
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredFileName, String containedFileName, Class<D> type, ScannerContext context) {
        return resolve(requiredFileName, type, false, context);
    }

    @Override
    public <D extends FileDescriptor> D match(String containedFileName, Class<D> type, ScannerContext context) {
        return resolve(containedFileName, type, true, context);
    }

    private <D extends FileDescriptor> D resolve(String requiredFileName, Class<D> type, boolean isMatch, ScannerContext context) {
        D fileDescriptor = getOrCreateAs(requiredFileName, type, fileName -> {
            try (Result<Result.CompositeRowObject> result = context.getStore()
                .executeQuery("MATCH (file:File:Local) WHERE file.fileName=$fileName RETURN file", Map.of("fileName", fileName))) {
                return result.hasResult() ?
                    result.getSingleResult()
                        .get("file", FileDescriptor.class) :
                    null;
            }
        }, isMatch, context);
        return context.getStore()
            .addDescriptorType(fileDescriptor, LocalDescriptor.class)
            .as(type);
    }

}
