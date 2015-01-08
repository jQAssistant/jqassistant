package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexUtils;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

/**
 * A scanner for (remote) maven repositories.
 * 
 * @author pherklotz
 */
public class MavenRepositoryScannerPlugin extends AbstractScannerPlugin<URL, MavenRepositoryDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRepositoryScannerPlugin.class);

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return MavenScope.REPOSITORY == scope;
    }

    private MavenRepositoryDescriptor getRepositoryDescriptor(Store store, String url) {
        MavenRepositoryDescriptor repoDescriptor;
        repoDescriptor = store.create(MavenRepositoryDescriptor.class);
        repoDescriptor.setUrl(url);
        return repoDescriptor;
    }

    private void migrateToArtifactAndSetRelation(Store store, MavenRepositoryDescriptor repoDescriptor, String lastModified, Descriptor descriptor) {
        RepositoryArtifactDescriptor artifactDescriptor = store.migrate(descriptor, RepositoryArtifactDescriptor.class);

        repoDescriptor.getContainedArtifacts().add(artifactDescriptor);
        // ContainsDescriptor containsDescriptor = store.create(repoDescriptor,
        // ContainsDescriptor.class, artifactDescriptor);
        // containsDescriptor.setLastModified(lastModified);
    }

    /**
     * Resolves, scans and add the artifact to the
     * {@link MavenRepositoryDescriptor}.
     * 
     * @param scanner
     *            the {@link Scanner}
     * @param repoDescriptor
     *            the {@link MavenRepositoryDescriptor}
     * @param artifactResolver
     *            the {@link ArtifactResolver}
     * @param artifactInfo
     *            informations about the searches artifact
     * @throws IOException
     */
    private void resolveAndScan(Scanner scanner, MavenRepositoryDescriptor repoDescriptor, ArtifactResolver artifactResolver, ArtifactInfo artifactInfo)
            throws IOException {
        try {
            Store store = scanner.getContext().getStore();

            String groupId = artifactInfo.getFieldValue(MAVEN.GROUP_ID);
            String artifactId = artifactInfo.getFieldValue(MAVEN.ARTIFACT_ID);
            String packaging = artifactInfo.getFieldValue(MAVEN.PACKAGING);
            String version = artifactInfo.getFieldValue(MAVEN.VERSION);

            File artifactFile = artifactResolver.downloadArtifact(groupId, artifactId, packaging, version);
            try (FileResource fileResource = new DefaultFileResource(artifactFile)) {
                Descriptor descriptor = scanner.scan(fileResource, artifactFile.getAbsolutePath(), null);
                if (descriptor != null) {
                    String lastModified = artifactInfo.getFieldValue(MAVEN.LAST_MODIFIED);
                    migrateToArtifactAndSetRelation(store, repoDescriptor, lastModified, descriptor);
                } else {
                    LOGGER.debug("Could not scan artifact: " + artifactFile.getAbsoluteFile());
                }
            }
        } catch (ArtifactResolutionException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    @Override
    public MavenRepositoryDescriptor scan(URL item, String path, Scope scope, Scanner scanner) throws IOException {
        String username = MavenRepoCredentials.USERNAME;
        String password = MavenRepoCredentials.PASSWORD;

        Store store = scanner.getContext().getStore();
        IndexSearcher searcher = null;
        MavenRepositoryDescriptor repoDescriptor = null;
        // handles the remote maven index
        MavenIndex indexDownloader = null;
        try {
            indexDownloader = new MavenIndex(item, username, password);
            // the MavenRepositoryDescriptor
            repoDescriptor = getRepositoryDescriptor(store, item.toString());
            // used to resolve (remote) artifacts
            ArtifactResolver artifactResolver = new ArtifactResolver(item, username, password);
            searcher = indexDownloader.newIndexSearcher();
            IndexReader ir = searcher.getIndexReader();
            for (int i = 0; i < ir.maxDoc() && i < 100; i++) {
                Document doc = ir.document(i);
                ArtifactInfo ai = IndexUtils.constructArtifactInfo(doc, indexDownloader.getIndexingContext());
                if (ai == null) {
                    LOGGER.debug("Could not construct ArtifactInfo for document: " + doc.toString());
                    continue;
                }
                resolveAndScan(scanner, repoDescriptor, artifactResolver, ai);
            }
        } finally {
            if (searcher != null) {
                indexDownloader.closeIndexSearcher(searcher);
            }
        }

        return repoDescriptor;
    }
}
