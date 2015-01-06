package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.m2repo.api.model.ContainsDescriptor;
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

	@Override
	public boolean accepts(URL item, String path, Scope scope)
			throws IOException {
		return MavenScope.REPOSITORY == scope;
	}

	@Override
	public MavenRepositoryDescriptor scan(URL item, String path, Scope scope,
			Scanner scanner) throws IOException {
		Store store = scanner.getContext().getStore();
		ArtifactDownloader artifactDownloader = new ArtifactDownloader(item,
				MavenRepoCredentials.USERNAME, MavenRepoCredentials.PASSWORD);
		MavenIndexDownloader indexDownloader;
		try {
			indexDownloader = new MavenIndexDownloader(item);
		} catch (IllegalArgumentException | PlexusContainerException
				| ComponentLookupException e1) {
			throw new IOException(e1);
		}

		IndexingContext indexingContext = indexDownloader.getIndexingContext();
		IndexSearcher searcher = indexingContext.acquireIndexSearcher();
		MavenRepositoryDescriptor repoDescriptor = null;
		try {
			repoDescriptor = store.create(MavenRepositoryDescriptor.class);
			repoDescriptor.setUrl(item.toString());
			IndexReader ir = searcher.getIndexReader();
			for (int i = 0; i < ir.maxDoc() && i < 10; i++) {
				Document doc = ir.document(i);
				ArtifactInfo ai = IndexUtils.constructArtifactInfo(doc,
						indexingContext);
				if (ai != null) {
					final File artifactFile = artifactDownloader
							.downloadArtifact(ai.getFieldValue(MAVEN.GROUP_ID),
									ai.getFieldValue(MAVEN.ARTIFACT_ID),
									ai.getFieldValue(MAVEN.PACKAGING),
									ai.getFieldValue(MAVEN.VERSION));
					System.out.println("File downloaded: "
							+ artifactFile.getAbsolutePath());
					try (AbstractFileResource fileResource = new AbstractFileResource() {
						@Override
						public InputStream createStream() throws IOException {
							return new BufferedInputStream(new FileInputStream(
									artifactFile));
						}
					}) {
						Descriptor descriptor = scanner.scan(fileResource,
								artifactFile.getAbsolutePath(), null);
						if (descriptor != null) {
							RepositoryArtifactDescriptor artifactDescriptor = store
									.migrate(descriptor,
											RepositoryArtifactDescriptor.class);

							ContainsDescriptor containsDescriptor = store
									.create(repoDescriptor,
											ContainsDescriptor.class,
											artifactDescriptor);
							containsDescriptor.setLastModified(ai
									.getFieldValue(MAVEN.LAST_MODIFIED));
						}
					} catch (Exception e) {
						throw e;
					}
				}
			}
		} catch (ArtifactResolutionException e) {
			throw new IOException(e);
		} finally {
			indexingContext.releaseIndexSearcher(searcher);
		}

		return repoDescriptor;
	}
}
