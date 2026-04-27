package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file resolver strategy for file containers.
 */
public class ContainerFileResolver extends AbstractFileResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerFileResolver.class);

    private static final String CACHE_KEY = ContainerFileResolver.class.getName();

    private DirectoryDescriptor directoryDescriptor;

    private final ScannerContext scannerContext;

    private final Map<String, FileDescriptor> requiredFiles;

    private final Map<String, FileDescriptor> providedFiles;

    public ContainerFileResolver(ScannerContext scannerContext, DirectoryDescriptor directoryDescriptor) {
        super(CACHE_KEY + "/" + directoryDescriptor.getId());
        this.directoryDescriptor = directoryDescriptor;
        this.scannerContext = scannerContext;
        if (directoryDescriptor instanceof ArtifactFileDescriptor) {
            ArtifactFileDescriptor artifactFileDescriptor = (ArtifactFileDescriptor) directoryDescriptor;
            this.providedFiles = getCache(artifactFileDescriptor.getProvides());
            this.requiredFiles = getCache(artifactFileDescriptor.getRequires());
        } else {
            this.providedFiles = new HashMap<>();
            this.requiredFiles = new HashMap<>();
        }
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, String containedPath, Class<D> type, ScannerContext context) {
        final FileDescriptor fileDescriptor = providedFiles.get(containedPath);
        D result;
        if (fileDescriptor != null) {
            result = getOrCreateAs(containedPath, type, path -> fileDescriptor, context);
            providedFiles.put(containedPath, result);
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
        if (directoryDescriptor instanceof ArtifactFileDescriptor) {
            ArtifactFileDescriptor artifactFileDescriptor = (ArtifactFileDescriptor) directoryDescriptor;
            sync(artifactFileDescriptor.getRequires(), requiredFiles);
            sync(artifactFileDescriptor.getProvides(), providedFiles);
        }
        // to be removed in 3.0 to avoid ambiguity, see https://github.com/jQAssistant/jqassistant/issues/1093
        sync(directoryDescriptor.getContains(), providedFiles);
        scannerContext.getStore()
            .invalidateCache(CACHE_KEY);
    }

    /**
     * Sync the given target collection with the new state from the cache map.
     *
     * @param target
     *     The target collection.
     * @param after
     *     The new state to sync to.
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
     *     The collection of file descriptors.
     * @return The cache map.
     */
    private Map<String, FileDescriptor> getCache(Iterable<FileDescriptor> fileDescriptors) {
        Map<String, FileDescriptor> cache = new HashMap<>();
        for (Descriptor descriptor : fileDescriptors) {
            if (descriptor instanceof FileDescriptor) {
                FileDescriptor fileDescriptor = (FileDescriptor) descriptor;
                cache.put(fileDescriptor.getFileName(), fileDescriptor);
            } else {
                LOGGER.warn("{} is not a file descriptor, container={}", descriptor, directoryDescriptor);
            }
        }
        return cache;
    }

    /**
     * Build the hierarchy of the container entries, i.e. add contains relations
     * from containers to their children.
     */
    private void createHierarchy() {
        for (Map.Entry<String, FileDescriptor> entry : providedFiles.entrySet()) {
            String relativePath = entry.getKey();
            FileDescriptor fileDescriptor = entry.getValue();
            int separatorIndex = relativePath.lastIndexOf('/');
            if (separatorIndex != -1) {
                String parentName = relativePath.substring(0, separatorIndex);
                FileDescriptor parentDescriptor = providedFiles.get(parentName);
                if (parentDescriptor instanceof DirectoryDescriptor) {
                    ((DirectoryDescriptor) parentDescriptor).getContains()
                        .add(fileDescriptor);
                }
            }
        }
    }

    /**
     * Adds a file to the container.
     *
     * @param path
     *     The path of the file.
     * @param fileDescriptor
     *     The file descriptor.
     */
    public void put(String path, FileDescriptor fileDescriptor) {
        providedFiles.put(path, fileDescriptor);
    }

    /**
     * Returns the size of the container.
     *
     * @return The size of the container.
     */
    public int size() {
        return providedFiles.size();
    }
}
