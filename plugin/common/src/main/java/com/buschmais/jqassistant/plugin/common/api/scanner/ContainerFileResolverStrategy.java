package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResolverStrategy;

/**
 * A file resolver strategy for file containers.
 */
public class ContainerFileResolverStrategy extends AbstractFileResolverStrategy {

    private Map<String, FileDescriptor> requiredFiles = new HashMap<>();

    private Map<String, FileDescriptor> containedFiles = new HashMap<>();

    private FileContainerDescriptor fileContainerDescriptor;

    public ContainerFileResolverStrategy(FileContainerDescriptor fileContainerDescriptor) {
        this.fileContainerDescriptor = fileContainerDescriptor;
    }

    @Override
    public <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context) {
        D result;
        FileDescriptor fileDescriptor = containedFiles.get(path);
        if (fileDescriptor != null) {
            result = toFileDescriptor(fileDescriptor, type, path, context);
            containedFiles.put(path, result);
        } else {
            fileDescriptor = requiredFiles.get(path);
            result = toFileDescriptor(fileDescriptor, type, path, context);
            requiredFiles.put(path, result);
        }
        return result;
    }

    @Override
    public <D extends FileDescriptor> D match(String path, Class<D> type, ScannerContext context) {
        FileDescriptor fileDescriptor = requiredFiles.remove(path);
        if (fileDescriptor != null) {
            return toFileDescriptor(fileDescriptor, type, path, context);
        }
        return null;
    }

    public void flush() {
        createHierarchy();
        fileContainerDescriptor.getRequires().addAll(requiredFiles.values());
        fileContainerDescriptor.getContains().addAll(containedFiles.values());
    }

    /**
     * Build the hierarchy of the container entries, i.e. add contains relations
     * from containers to their children.
     */
    private void createHierarchy() {
        for (Map.Entry<String, FileDescriptor> entry : containedFiles.entrySet()) {
            String relativePath = entry.getKey();
            FileDescriptor fileDescriptor = entry.getValue();
            int separatorIndex = relativePath.lastIndexOf('/');
            if (separatorIndex != -1) {
                String parentName = relativePath.substring(0, separatorIndex);
                FileDescriptor parentDescriptor = containedFiles.get(parentName);
                if (parentDescriptor instanceof FileContainerDescriptor) {
                    ((FileContainerDescriptor) parentDescriptor).getContains().add(fileDescriptor);
                }
            }
        }
    }

    public void put(String path, FileDescriptor fileDescriptor) {
        containedFiles.put(path, fileDescriptor);
    }

    public int size() {
        return containedFiles.size();
    }
}
