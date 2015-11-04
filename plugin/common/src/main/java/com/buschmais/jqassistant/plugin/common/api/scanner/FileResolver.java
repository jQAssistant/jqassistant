package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.Deque;
import java.util.LinkedList;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * A file resolver.
 */
public class FileResolver {

    /**
     * The registered file resolver instances identified by their type.
     */
    private Deque<FileResolverStrategy> resolverStrategies = new LinkedList<>();

    /**
     * Add a file resolver.
     * 
     * @param fileResolverStrategy
     *            A file resolver.
     */
    public void push(FileResolverStrategy fileResolverStrategy) {
        resolverStrategies.push(fileResolverStrategy);
    }

    /**
     * Remove a file resolver.
     */
    public void pop() {
        resolverStrategies.pop();
    }

    public FileDescriptor create(String path, ScannerContext context) {
        for (FileResolverStrategy fileResolverStrategy : resolverStrategies) {
            FileDescriptor fileDescriptor = fileResolverStrategy.match(path, FileDescriptor.class, context);
            if (fileDescriptor != null) {
                return fileDescriptor;
            }
        }
        return createFileDescriptor(path, FileDescriptor.class, context);
    }

    public <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context) {
        for (FileResolverStrategy fileResolverStrategy : resolverStrategies) {
            D fileDescriptor = fileResolverStrategy.require(path, type, context);
            if (fileDescriptor != null) {
                return fileDescriptor;
            }
        }
        return createFileDescriptor(path, type, context);
    }

    private <D extends FileDescriptor> D createFileDescriptor(String path, Class<D> type, ScannerContext context) {
        D fileDescriptor = context.getStore().create(type);
        fileDescriptor.setFileName(path);
        return fileDescriptor;
    }

}
