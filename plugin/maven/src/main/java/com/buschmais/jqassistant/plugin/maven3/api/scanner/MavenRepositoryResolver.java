package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenRepositoryDescriptor;

public class MavenRepositoryResolver {

    private MavenRepositoryResolver() {
    }

    /**
     * Finds or creates a repository descriptor for the given url.
     *
     * @param store
     *            the {@link Store}
     * @param url
     *            the repository url
     * @return a {@link MavenRepositoryDescriptor} for the given url.
     */
    public static MavenRepositoryDescriptor resolve(Store store, String url) {
        MavenRepositoryDescriptor repositoryDescriptor = store.find(MavenRepositoryDescriptor.class, url);
        if (repositoryDescriptor == null) {
            repositoryDescriptor = store.create(MavenRepositoryDescriptor.class);
            repositoryDescriptor.setUrl(url);
        }
        return repositoryDescriptor;
    }

}
