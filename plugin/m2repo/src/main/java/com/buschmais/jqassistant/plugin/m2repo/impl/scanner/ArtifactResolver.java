package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transfers artifacts from a remote repository to a local repository.
 * 
 * @author pherklotz
 */
public class ArtifactResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResolver.class);

    private final RemoteRepository repository;
    private final RepositorySystem repositorySystem;

    private final DefaultRepositorySystemSession session;

    private final URL repositoryUrl;

    /**
     * Creates a new object.
     * 
     * @param repositoryUrl
     *            the repository url
     */
    public ArtifactResolver(URL repositoryUrl) {
        this(repositoryUrl, null, null);
    }

    /**
     * Creates a new object.
     * 
     * @param repositoryUrl
     *            the repository url
     * @param username
     *            an username for authentication
     * @param password
     *            a password for authentication
     */
    public ArtifactResolver(URL repositoryUrl, String username, String password) {
        this.repositoryUrl = repositoryUrl;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create new " + this.getClass().getSimpleName() + " for URL " + repositoryUrl.toString());
        }
        AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        if (username != null) {
            authBuilder.addUsername(username);
        }
        if (password != null) {
            authBuilder.addPassword(password);
        }
        Authentication auth = authBuilder.build();
        repository = new RemoteRepository.Builder("jqa", "default", repositoryUrl.toString()).setAuthentication(auth).build();
        repositorySystem = newRepositorySystem();
        session = newRepositorySystemSession(repositorySystem);
    }

    /**
     * Resolves an artifact with the given properties and transfers it in a
     * local repository.
     * 
     * @param groupId
     *            the artifact groupId
     * @param artifactId
     *            the artifact artifactId
     * @param type
     *            the artifact type (e.g. jar, pom, ...)
     * @param version
     *            the artifact version
     * @return the local file handle
     * @throws ArtifactResolutionException
     *             in case of a unresolvable artifacts
     */
    public File downloadArtifact(String groupId, String artifactId, String type, String version) throws ArtifactResolutionException {

        if (type == null) {
            type = "jar";
        }

        final String gav = String.format("%s:%s:%s:%s", groupId, artifactId, type, version);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Download artifact: " + gav);
        }
        ArtifactRequest artifactRequest = newArtifactRequest(gav);

        ArtifactResult artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);

        return artifactResult.getArtifact().getFile();
    }

    /**
     * Creates a new {@link ArtifactRequest} Object with the artifact GAV and
     * the repository.
     * 
     * @param artifactGav
     * @return
     */
    private ArtifactRequest newArtifactRequest(String artifactGav) {
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(new DefaultArtifact(artifactGav));
        artifactRequest.setRepositories(Arrays.asList(repository));
        return artifactRequest;
    }

    /**
     * Creates a new {@link RepositorySystem} object.
     * 
     * @return the new object
     */
    private RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService(RepositorySystem.class);
    }

    /**
     * Creates a new {@link RepositorySystemSession}.
     * 
     * @param system
     *            the {@link RepositorySystem}
     * @return a new {@link RepositorySystemSession}.
     */
    private DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository("target/local-repo/" + repositoryUrl.getHost());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        return session;
    }
}
