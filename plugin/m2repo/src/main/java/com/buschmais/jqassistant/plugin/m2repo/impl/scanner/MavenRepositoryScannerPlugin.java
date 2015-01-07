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
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
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
public class MavenRepositoryScannerPlugin extends
		AbstractScannerPlugin<URL, MavenRepositoryDescriptor> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MavenRepositoryScannerPlugin.class);

	@Override
	public boolean accepts(URL item, String path, Scope scope)
			throws IOException {
		return MavenScope.REPOSITORY == scope;
	}

	private MavenRepositoryDescriptor getRepositoryDescriptor(Store store,
			String url) {
		MavenRepositoryDescriptor repoDescriptor;
		repoDescriptor = store.create(MavenRepositoryDescriptor.class);
		repoDescriptor.setUrl(url);
		return repoDescriptor;
	}

	private void migrateToArtifactAndSetRelation(Store store,
			MavenRepositoryDescriptor repoDescriptor, String lastModified,
			Descriptor descriptor) {
		RepositoryArtifactDescriptor artifactDescriptor = store.migrate(
				descriptor, RepositoryArtifactDescriptor.class);

		repoDescriptor.getArtifacts().add(artifactDescriptor);
		// ContainsDescriptor containsDescriptor = store.create(repoDescriptor,
		// ContainsDescriptor.class, artifactDescriptor);
		// containsDescriptor.setLastModified(lastModified);
	}

	@Override
	public MavenRepositoryDescriptor scan(URL item, String path, Scope scope,
			Scanner scanner) throws IOException {
		Store store = scanner.getContext().getStore();
		IndexSearcher searcher = null;
		MavenRepositoryDescriptor repoDescriptor = null;
		// handles the remote maven index
		MavenIndexDownloader indexDownloader = null;
		try {
			indexDownloader = new MavenIndexDownloader(item,
					MavenRepoCredentials.USERNAME,
					MavenRepoCredentials.PASSWORD);
			// the MavenRepositoryDescriptor
			repoDescriptor = getRepositoryDescriptor(store, item.toString());
			// used to resolve (remote) artifacts
			ArtifactResolver artifactDownloader = new ArtifactResolver(item,
					MavenRepoCredentials.USERNAME,
					MavenRepoCredentials.PASSWORD);
			searcher = indexDownloader.newIndexSearcher();
			IndexReader ir = searcher.getIndexReader();
			for (int i = 0; i < ir.maxDoc() && i < 5_000; i++) {
				Document doc = ir.document(i);
				ArtifactInfo ai = IndexUtils.constructArtifactInfo(doc,
						indexDownloader.getIndexingContext());
				if (ai != null) {
					File artifactFile = null;
					try {
						artifactFile = artifactDownloader.downloadArtifact(
								ai.getFieldValue(MAVEN.GROUP_ID),
								ai.getFieldValue(MAVEN.ARTIFACT_ID),
								ai.getFieldValue(MAVEN.PACKAGING),
								ai.getFieldValue(MAVEN.VERSION));
						try (FileResource fileResource = new ArtifactFileResource(
								artifactFile)) {
							Descriptor descriptor = scanner.scan(fileResource,
									artifactFile.getAbsolutePath(), null);
							if (descriptor != null) {
								migrateToArtifactAndSetRelation(store,
										repoDescriptor,
										ai.getFieldValue(MAVEN.LAST_MODIFIED),
										descriptor);
							} else {
								LOGGER.debug("Could not scan artifact: "
										+ artifactFile.getAbsoluteFile());
							}
						}
					} catch (ArtifactResolutionException e) {
						LOGGER.warn(e.getMessage());
					}
				} else {
					LOGGER.debug("Could not construct ArtifactInfo for document: "
							+ doc.toString());
				}
			}
		} catch (IllegalArgumentException | PlexusContainerException
				| ComponentLookupException e) {
			throw new IOException(e);
		} finally {
			if (searcher != null) {
				indexDownloader.closeIndexSearcher(searcher);
			}
		}

		return repoDescriptor;
	}
}
