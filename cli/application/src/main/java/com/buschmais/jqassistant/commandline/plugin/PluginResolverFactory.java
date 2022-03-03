package com.buschmais.jqassistant.commandline.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.configuration.Remote;
import com.buschmais.jqassistant.commandline.configuration.Repositories;
import com.buschmais.jqassistant.core.plugin.api.PluginResolver;
import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;
import com.buschmais.jqassistant.core.plugin.impl.AetherPluginResolverImpl;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

/**
 * Factory for the {@link PluginResolver} to be used in standalone in the CLI.
 */
@Slf4j
public class PluginResolverFactory {

    private static final RemoteRepository CENTRAL = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2").build();

    private File jqassistantUserDir;

    public PluginResolverFactory() {
        File userHome = new File(System.getProperty("user.home"));
        this.jqassistantUserDir = new File(userHome, ".jqassistant");
    }

    /**
     * Creates a {@link PluginResolver} using the given {@link CliConfiguration} providing the required {@link Plugin}s and {@link Repositories}
     * <p>
     * The default local repository is ~/.jqassistant/repository.
     * <p>
     * If no remote repository is specified the resolver will use Maven Central, see {@link #CENTRAL}.
     *
     * @param configuration
     *     The {@link CliConfiguration}.
     * @return The {@link PluginResolver}.
     */
    public PluginResolver create(CliConfiguration configuration) {
        Optional<Repositories> repositories = configuration.repositories();
        File localRepository = getLocalRepository(repositories);
        List<RemoteRepository> remoteRepositories = getRemoteRepositories(repositories);

        RepositorySystem repositorySystem = newRepositorySystem();
        log.info("Using local repository '{}'.", localRepository);
        RepositorySystemSession session = newRepositorySystemSession(repositorySystem, localRepository);

        return new AetherPluginResolverImpl(repositorySystem, session, remoteRepositories);
    }

    /**
     * Determine the local repository to use.
     *
     * @param repositories
     *     The {@link Repositories} configuration.
     * @return The local repository.
     */
    private File getLocalRepository(Optional<Repositories> repositories) {
        // determine local repository
        File localRepository = repositories.map(r -> r.local()).orElse(empty()).orElseGet(() -> {
            File repository = new File(jqassistantUserDir, "repository");
            repository.mkdirs();
            return repository;
        });
        return localRepository;
    }

    /**
     * Determines the remote repositories to use, using Maven Central as fallback.
     *
     * @param repositories
     *     The {@link Repositories} configuration.
     * @return The list of configured {@link RemoteRepository}s.
     */
    private List<RemoteRepository> getRemoteRepositories(Optional<Repositories> repositories) {
        Map<String, Remote> remotes = repositories.map(r -> r.remotes()).orElse(emptyMap());
        if (remotes.isEmpty()) {
            return singletonList(CENTRAL);
        }
        return remotes.entrySet().stream().map(remoteEntry -> {
            String id = remoteEntry.getKey();
            Remote remote = remoteEntry.getValue();
            AuthenticationBuilder authBuilder = new AuthenticationBuilder();
            remote.username().ifPresent(username -> authBuilder.addUsername(username));
            remote.password().ifPresent(password -> authBuilder.addPassword(password));
            return new RemoteRepository.Builder(id, "default", remote.url()).setAuthentication(authBuilder.build()).build();
        }).collect(toList());
    }

    /**
     * Creates a new {@link RepositorySystem}.
     *
     * @return The {@link RepositorySystem}.
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
     *     the {@link RepositorySystem}
     * @return a new {@link RepositorySystemSession}.
     */
    private RepositorySystemSession newRepositorySystemSession(RepositorySystem system, File localDirectory) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setTransferListener(new TransferListener());
        LocalRepository localRepo = new LocalRepository(localDirectory);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        return session;
    }

    /**
     * A transfer listener logging transfer events.
     */
    private static class TransferListener extends AbstractTransferListener {
        @Override
        public void transferStarted(TransferEvent transferEvent) {
            log.info("Downloading '{}'.", transferEvent.getResource().getFile().getName());
        }

        @Override
        public void transferSucceeded(TransferEvent transferEvent) {
            log.info("Finished download of '{}'.", transferEvent.getResource().getFile().getName());
        }
    }
}
