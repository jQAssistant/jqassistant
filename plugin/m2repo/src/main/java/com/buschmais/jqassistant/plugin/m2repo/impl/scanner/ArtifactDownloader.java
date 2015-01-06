package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

public class ArtifactDownloader {

	public static void main(String[] args) {
		try {
			File file = new ArtifactDownloader(new URL(
					MavenRepoCredentials.REPO_URL),
					MavenRepoCredentials.USERNAME,
					MavenRepoCredentials.PASSWORD).downloadArtifact(
					"com.buschmais.tinkerforge4jenkins",
					"tinkerforge4jenkins-core", "jar", "1.0.0");
			System.out.println(file.getAbsolutePath());
		} catch (ArtifactResolutionException | MalformedURLException e) {
			e.printStackTrace();
		}

	}

	private final RemoteRepository repository;
	private final RepositorySystem repositorySystem;

	private final DefaultRepositorySystemSession session;

	public ArtifactDownloader(URL repositoryUrl) {
		this(repositoryUrl, null, null);
	}

	public ArtifactDownloader(URL repositoryUrl, String username,
			String password) {
		AuthenticationBuilder authBuilder = new AuthenticationBuilder();
		if (username != null) {
			authBuilder.addUsername(username);
		}
		if (password != null) {
			authBuilder.addPassword(password);
		}
		Authentication auth = authBuilder.build();
		repository = new RemoteRepository.Builder("buschmais-public",
				"default", repositoryUrl.toString()).setAuthentication(auth)
				.build();
		repositorySystem = newRepositorySystem();
		session = newRepositorySystemSession(repositorySystem);
	}

	public File downloadArtifact(String groupId, String artifactId,
			String type, String version) throws ArtifactResolutionException {
		if (type == null) {
			type = "jar";
		}

		final String gav = String.format("%s:%s:%s:%s", groupId, artifactId,
				type, version);

		ArtifactRequest artifactRequest = newArtifactRequest(gav);

		ArtifactResult artifactResult = repositorySystem.resolveArtifact(
				session, artifactRequest);

		return artifactResult.getArtifact().getFile();
	}

	private ArtifactRequest newArtifactRequest(String artifactGav) {
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(new DefaultArtifact(artifactGav));
		artifactRequest.setRepositories(Arrays.asList(repository));
		return artifactRequest;
	}

	private RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils
				.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class,
				BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class,
				FileTransporterFactory.class);
		locator.addService(TransporterFactory.class,
				HttpTransporterFactory.class);
		locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl,
					Throwable exception) {
				exception.printStackTrace();
			}
		});

		return locator.getService(RepositorySystem.class);
	}

	private DefaultRepositorySystemSession newRepositorySystemSession(
			RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils
				.newSession();

		LocalRepository localRepo = new LocalRepository("target/local-repo");
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(
				session, localRepo));

		return session;
	}
}
