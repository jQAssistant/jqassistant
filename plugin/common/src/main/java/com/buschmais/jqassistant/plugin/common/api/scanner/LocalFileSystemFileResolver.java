package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.api.Query.Result;

/**
 * File resolver for the local file system using absolute paths.
 * <p>
 */
public class LocalFileSystemFileResolver extends AbstractFileResolver {

    public LocalFileSystemFileResolver() {
        super(LocalFileSystemFileResolver.class.getName());
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, String containedPath, Class<D> type, ScannerContext context) {
        return resolve(requiredPath, type, context);
    }

    @Override
    public <D extends FileDescriptor> D match(String containedPath, Class<D> type, ScannerContext context) {
        return resolve(containedPath, type, context);
    }

    private <D extends FileDescriptor> D resolve(String requiredPath, Class<D> type, ScannerContext context) {
        return getOrCreateAs(requiredPath, type, s -> {
            Map<String, Object> params = new HashMap<>();
            params.put("fileName", s);
            Result<Result.CompositeRowObject> result = context.getStore().executeQuery("MATCH (file:File) WHERE file.fileName=$fileName RETURN file", params);
            return result.hasResult() ? result.getSingleResult().get("file", FileDescriptor.class) : null;
        }, context);
    }

}
