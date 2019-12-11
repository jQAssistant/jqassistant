package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractFileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenRepositoryDescriptor;

/**
 * A file resolver strategy for a local maven repository.
 * <p>
 * If a file is given which is part of the local maven repository then this
 * strategy will lookup an existing artifact descriptor.
 */
public class MavenRepositoryFileResolver extends AbstractFileResolver {

    private MavenRepositoryDescriptor repositoryDescriptor;

    /**
     * Constructor.
     *
     * @param repositoryDescriptor The descriptor representing the repository.
     */
    public MavenRepositoryFileResolver(MavenRepositoryDescriptor repositoryDescriptor) {
        this.repositoryDescriptor = repositoryDescriptor;
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, String containedPath, Class<D> type, ScannerContext context) {
        return match(requiredPath, type, context);
    }

    @Override
    public <D extends FileDescriptor> D match(String containedPath, Class<D> type, ScannerContext context) {
        return getOrCreateAs(containedPath, type, path -> repositoryDescriptor.findFile(containedPath), context);
    }
}
