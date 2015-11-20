package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
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

import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.m2repo.api.ArtifactProvider;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;

/**
 * Transfers artifacts from a remote repository to a local repository.
 * 
 * @author pherklotz
 */
public class AetherArtifactProvider implements ArtifactProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AetherArtifactProvider.class);

    private MavenRepositoryDescriptor repositoryDescriptor;

    private URL url;

    private String username;

    private String password;

    private final File repositoryRoot;

    private MavenRepositoryFileResolver repositoryFileResolver;

    private MavenRepositoryArtifactResolver repositoryArtifactResolver;

    private final RemoteRepository repository;
    private final RepositorySystem repositorySystem;

    private final DefaultRepositorySystemSession session;

    /**
     * Creates a new object.
     * 
     * @param repositoryUrl
     *            The repository url
     * @param repositoryDescriptor
     *            The repository descriptor.
     * @param workDirectory
     *            The work directory for local caching of files.
     */
    public AetherArtifactProvider(URL repositoryUrl, MavenRepositoryDescriptor repositoryDescriptor, File workDirectory) {
        this.url = repositoryUrl;
        this.repositoryDescriptor = repositoryDescriptor;
        String userInfo = repositoryUrl.getUserInfo();
        this.username = StringUtils.substringBefore(userInfo, ":");
        this.password = StringUtils.substringAfter(userInfo, ":");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create new " + this.getClass().getSimpleName() + " for URL " + url);
        }
        AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        if (username != null) {
            authBuilder.addUsername(username);
        }
        if (password != null) {
            authBuilder.addPassword(password);
        }
        Authentication auth = authBuilder.build();
        String url = StringUtils.replace(repositoryUrl.toString(), repositoryUrl.getUserInfo() + "@", StringUtils.EMPTY);
        String repositoryId = DigestUtils.md5Hex(repositoryUrl.toString());
        repository = new RemoteRepository.Builder(repositoryId, "default", url).setAuthentication(auth).build();
        repositorySystem = newRepositorySystem();
        this.repositoryFileResolver = new MavenRepositoryFileResolver(repositoryDescriptor);
        this.repositoryRoot = new File(workDirectory, repositoryId);
        this.repositoryArtifactResolver = new MavenRepositoryArtifactResolver(repositoryRoot, repositoryFileResolver);
        LOGGER.debug("Using '{}' for repository URL '{}'", repositoryRoot, repositoryUrl);
        session = newRepositorySystemSession(repositorySystem, repositoryRoot);
    }

    @Override
    public MavenRepositoryDescriptor getRepositoryDescriptor() {
        return repositoryDescriptor;
    }

    /**
     * Resolves the given artifact.
     *
     * @param artifact
     *            the artifact to resolve
     * @return the local file handle
     * @throws ArtifactResolutionException
     *             in case of a unresolvable artifacts
     */
    @Override
    public ArtifactResult getArtifact(Artifact artifact) throws ArtifactResolutionException {
        ArtifactRequest artifactRequest = createArtifactRequest(artifact);
        return repositorySystem.resolveArtifact(session, artifactRequest);
    }

    @Override
    public FileResolver getFileResolver() {
        return repositoryFileResolver;
    }

    @Override
    public ArtifactResolver getArtifactResolver() {
        return repositoryArtifactResolver;
    }

    /**
     * Return the index of the remote repository.
     *
     * @return The index.
     * @throws IOException
     *             If the local index directoy cannot be created.
     */
    public MavenIndex getMavenIndex() throws IOException {
        File indexRoot = new File(repositoryRoot, ".index");
        return new MavenIndex(url, indexRoot, indexRoot, username, password);
    }

    /**
     * Creates a list of {@link ArtifactRequest}s for each artifact. The result
     * will always include the "pom" artifact for building the model.
     *
     * @param artifact
     *            The artifact.
     * @return The list of artifacts to retrieve.
     */
    private ArtifactRequest createArtifactRequest(Artifact artifact) {
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        final List<RemoteRepository> repositories = Collections.singletonList(repository);
        artifactRequest.setRepositories(repositories);
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
    private DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, File localDirectory) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(localDirectory);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        return session;
    }

}
