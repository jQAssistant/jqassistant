package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.index.Indexer;
import org.apache.maven.index.context.ExistingLuceneIndexMismatchException;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.AbstractTransferListener;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class MavenIndexDownloader {

	private IndexingContext indexingContext;

	private PlexusContainer plexusContainer;

	public MavenIndexDownloader(URL repoUrl)
			throws ExistingLuceneIndexMismatchException,
			IllegalArgumentException, PlexusContainerException,
			ComponentLookupException, IOException {
		createIndexingContext(repoUrl);
		updateIndex();
	}

	private void createIndexingContext(URL repoUrl)
			throws PlexusContainerException, ComponentLookupException,
			ExistingLuceneIndexMismatchException, IllegalArgumentException,
			IOException {
		plexusContainer = new DefaultPlexusContainer();
		// lookup the indexer components from plexus
		Indexer indexer = plexusContainer.lookup(Indexer.class);
		// Files where local cache is (if any) and Lucene Index should be
		// located
		File localArtifactCache = new File("target/repo-artifact-cache");
		File localIndexDir = new File("target/repo-index");
		// Creators we want to use (search for fields it defines)
		List<IndexCreator> indexers = new ArrayList<>();
		indexers.add(plexusContainer.lookup(IndexCreator.class,
				MinimalArtifactInfoIndexCreator.ID));
		indexers.add(plexusContainer.lookup(IndexCreator.class,
				JarFileContentsIndexCreator.ID));
		indexers.add(plexusContainer.lookup(IndexCreator.class,
				MavenPluginArtifactInfoIndexCreator.ID));
		indexers.add(plexusContainer.lookup(IndexCreator.class,
				MavenArchetypeArtifactInfoIndexCreator.ID));

		// Create context for central repository index
		indexingContext = indexer.createIndexingContext("jqassistant-context",
				"jqassistant-analysis", localArtifactCache, localIndexDir,
				repoUrl.toString(), null, true, true, indexers);
	}

	public IndexingContext getIndexingContext() {
		return indexingContext;
	}

	private void updateIndex() throws ComponentLookupException, IOException {
		IndexUpdater indexUpdater = plexusContainer.lookup(IndexUpdater.class);
		Wagon httpWagon = plexusContainer.lookup(Wagon.class, "http");

		System.out.println("Updating Index...");
		System.out
				.println("This might take a while on first run, so please be patient!");
		// Create ResourceFetcher implementation to be used with
		// IndexUpdateRequest Here, we use Wagon based one as shorthand, but
		// all we need is a ResourceFetcher implementation
		TransferListener listener = new AbstractTransferListener() {
			@Override
			public void transferCompleted(TransferEvent transferEvent) {
				System.out.println(" - Done");
			}

			@Override
			public void transferProgress(TransferEvent transferEvent,
					byte[] buffer, int length) {
			}

			@Override
			public void transferStarted(TransferEvent transferEvent) {
				System.out.print(" Downloading "
						+ transferEvent.getResource().getName());
			}
		};
		AuthenticationInfo info = new AuthenticationInfo();
		info.setUserName(MavenRepoCredentials.USERNAME);
		info.setPassword(MavenRepoCredentials.PASSWORD);
		ResourceFetcher resourceFetcher = new WagonHelper.WagonFetcher(
				httpWagon, listener, info, null);
		Date centralContextCurrentTimestamp = indexingContext.getTimestamp();
		IndexUpdateRequest updateRequest = new IndexUpdateRequest(
				indexingContext, resourceFetcher);
		IndexUpdateResult updateResult = indexUpdater
				.fetchAndUpdateIndex(updateRequest);
		if (updateResult.isFullUpdate()) {
			System.out.println("Full update happened!");
		} else if (updateResult.getTimestamp().equals(
				centralContextCurrentTimestamp)) {
			System.out.println("No update needed, index is up to date!");
		} else {
			System.out.println("Incremental update happened, change covered "
					+ centralContextCurrentTimestamp + " - "
					+ updateResult.getTimestamp() + " period.");
		}
	}

}
