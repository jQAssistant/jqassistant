package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResolverStrategy;

import java.util.Deque;
import java.util.LinkedList;

/**
 * A file resolver.
 */
public class FileResolver {

    /**
     * The registered file resolver instances.
     */
    private Deque<FileResolverStrategy> resolverStrategies = new LinkedList<>();

    /**
     * Constructor.
     */
    public FileResolver() {
        resolverStrategies.push(new AbstractFileResolverStrategy() {
            @Override
            public <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context) {
                return toFileDescriptor(null, type, path, context);
            }

            @Override
            public <D extends FileDescriptor> D match(String path, Class<D> type, ScannerContext context) {
                return toFileDescriptor(null, type, path, context);
            }
        });
    }

    /**
     * Add a file resolver.
     *
     * @param fileResolverStrategy A file resolver.
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
        throw new IllegalStateException("No file resolver strategy available.");
    }

    public <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context) {
        for (FileResolverStrategy fileResolverStrategy : resolverStrategies) {
            D fileDescriptor = fileResolverStrategy.require(path, type, context);
            if (fileDescriptor != null) {
                return fileDescriptor;
            }
        }
        throw new IllegalStateException("No file resolver strategy available.");
    }


}
