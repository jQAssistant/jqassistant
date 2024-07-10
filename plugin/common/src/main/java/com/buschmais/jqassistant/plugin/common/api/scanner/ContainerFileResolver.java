package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file resolver strategy for file containers.
 */
public class ContainerFileResolver extends AbstractFileResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerFileResolver.class);

    private static final String CACHE_KEY = ContainerFileResolver.class.getName();

    private FileContainerDescriptor fileContainerDescriptor;

    private final ScannerContext scannerContext;

    private final Map<String, FileDescriptor> requiredFiles;

    private final Map<String, FileDescriptor> containedFiles;

    public ContainerFileResolver(ScannerContext scannerContext, FileContainerDescriptor fileContainerDescriptor) {
        super(CACHE_KEY + "/" + fileContainerDescriptor.getId());
        this.fileContainerDescriptor = fileContainerDescriptor;
        this.scannerContext = scannerContext;
        this.containedFiles = getCache(fileContainerDescriptor.getContains());
        this.requiredFiles = getCache(fileContainerDescriptor.getRequires());
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, String containedPath, Class<D> type, ScannerContext context) {
        final FileDescriptor fileDescriptor = containedFiles.get(containedPath);
        D result;
        if (fileDescriptor != null) {
            result = getOrCreateAs(containedPath, type, path -> fileDescriptor, context);
            containedFiles.put(containedPath, result);
        } else {
            result = getOrCreateAs(containedPath, type, path -> requiredFiles.get(containedPath), context);
            requiredFiles.put(containedPath, result);
        }
        return result;
    }

    @Override
    public <D extends FileDescriptor> D match(String containedPath, Class<D> type, ScannerContext context) {
        FileDescriptor fileDescriptor = requiredFiles.remove(containedPath);
        return getOrCreateAs(containedPath, type, path -> fileDescriptor, context);
    }

    /**
     * Flush the caches to the store.
     */
    public void flush() {
        createHierarchy();
        sync(fileContainerDescriptor.getRequires(), requiredFiles);
        sync(fileContainerDescriptor.getContains(), containedFiles);
        scannerContext.getStore().invalidateCache(CACHE_KEY);
    }

    /**
     * Sync the given target collection with the new state from the cache map.
     *
     * @param target
     *            The target collection.
     * @param after
     *            The new state to sync to.
     */
    private void sync(Collection<FileDescriptor> target, Map<String, FileDescriptor> after) {
        Map<String, FileDescriptor> before = getCache(target);
        Map<String, FileDescriptor> all = new HashMap<>();
        all.putAll(before);
        all.putAll(after);
        for (Map.Entry<String, FileDescriptor> entry : all.entrySet()) {
            String key = entry.getKey();
            FileDescriptor fileDescriptor = entry.getValue();
            boolean hasBefore = before.containsKey(key);
            boolean hasAfter = after.containsKey(key);
            if (hasBefore && !hasAfter) {
                target.remove(fileDescriptor);
            } else if (!hasBefore && hasAfter) {
                target.add(fileDescriptor);
            }
        }
    }

    /**
     * Creates cache map from the given collection of file descriptors.
     *
     * @param fileDescriptors
     *            The collection of file descriptors.
     * @return The cache map.
     */
    private Map<String, FileDescriptor> getCache(Iterable<FileDescriptor> fileDescriptors) {
        Map<String, FileDescriptor> cache = new HashMap<>();
        for (Descriptor descriptor : fileDescriptors) {
            if (descriptor instanceof FileDescriptor) {
                FileDescriptor fileDescriptor = (FileDescriptor) descriptor;
                cache.put(fileDescriptor.getFileName(), fileDescriptor);
            } else {
                LOGGER.warn("{} is not a file descriptor, container={}", descriptor, fileContainerDescriptor);
            }
        }
        return cache;
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

    /**
     * Adds a file to the container.
     *
     * @param path
     *            The path of the file.
     * @param fileDescriptor
     *            The file descriptor.
     */
    public void put(String path, FileDescriptor fileDescriptor) {
        containedFiles.put(path, fileDescriptor);
    }

    /**
     * Returns the size of the container.
     *
     * @return The size of the container.
     */
    public int size() {
        return containedFiles.size();
    }
}
