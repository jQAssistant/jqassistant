package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.Deque;
import java.util.LinkedList;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * A file resolver.
 */
public final class FileResolver {

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
            Descriptor fileDescriptor = fileResolverStrategy.match(path, context);
            if (fileDescriptor != null) {
                return toFileDescriptor(fileDescriptor, path, context);
            }
        }
        return createFileDescriptor(path, context);
    }

    public FileDescriptor find(String path, ScannerContext context) {
        for (FileResolverStrategy fileResolverStrategy : resolverStrategies) {
            Descriptor descriptor = fileResolverStrategy.require(path, context);
            if (descriptor != null) {
                return toFileDescriptor(descriptor, path, context);
            }
        }
        return createFileDescriptor(path, context);
    }

    private FileDescriptor toFileDescriptor(Descriptor descriptor, String path, ScannerContext context) {
        if (!(descriptor instanceof FileDescriptor)) {
            FileDescriptor fileDescriptor = context.getStore().addDescriptorType(descriptor, FileDescriptor.class);
            fileDescriptor.setFileName(path);
            return fileDescriptor;
        }
        return (FileDescriptor) descriptor;
    }

    private FileDescriptor createFileDescriptor(String path, ScannerContext context) {
        FileDescriptor fileDescriptor = context.getStore().create(FileDescriptor.class);
        fileDescriptor.setFileName(path);
        return fileDescriptor;
    }

}
