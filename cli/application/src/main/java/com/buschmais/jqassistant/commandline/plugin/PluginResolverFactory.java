package com.buschmais.jqassistant.commandline.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.configuration.*;
import com.buschmais.jqassistant.core.runtime.api.configuration.Plugin;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.plugin.AetherPluginResolverImpl;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_FAIL;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

/**
 * Factory for the {@link PluginResolver} to be used in standalone in the CLI.
 */
@Slf4j
public class PluginResolverFactory {

    private static final RepositoryPolicy SNAPSHOT_REPOSITORY_POLICY = new RepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_FAIL);

    private static final String CENTRAL_URL = "https://repo1.maven.org/maven2";
    public static final String CENTRAL_ID = "central";
    public static final String REPOSITORY_LAYOUT_DEFAULT = "default";

    private final File jqassistantUserDir;

    public PluginResolverFactory(File userHome) {
        this.jqassistantUserDir = new File(userHome, ".jqassistant");
    }

    /**
     * Creates a {@link PluginResolver} using the given {@link CliConfiguration} providing the required {@link Plugin}s and {@link Repositories}
     * <p>
     * The default local repository is ~/.jqassistant/repository.
     * <p>
     * If no remote repository is specified the resolver will use Maven Central, see {@link #CENTRAL_URL}.
     *
     * @param configuration
     *     The {@link CliConfiguration}.
     * @return The {@link PluginResolver}.
     */
    public PluginResolver create(CliConfiguration configuration) {
        Repositories repositories = configuration.repositories();
        File localRepository = getLocalRepository(repositories);
        Optional<org.eclipse.aether.repository.Proxy> proxy = getProxy(configuration.proxy());
        List<RemoteRepository> remoteRepositories = getRemoteRepositories(repositories, proxy);

        RepositorySystem repositorySystem = newRepositorySystem();
        log.info("Using local repository '{}' and remote repositories {}.", localRepository, remoteRepositories.stream()
            .map(remoteRepository -> "'" + remoteRepository.getId() + " (" + remoteRepository.getUrl() + ")'")
            .collect(joining(", ")));
        RepositorySystemSession session = newRepositorySystemSession(repositories, repositorySystem, localRepository);

        return new AetherPluginResolverImpl(repositorySystem, session, remoteRepositories);
    }

    /**
     * Determine the local repository to use.
     *
     * @param repositories
     *     The {@link Repositories} configuration.
     * @return The local repository.
     */
    private File getLocalRepository(Repositories repositories) {
        // determine local repository
        File localRepository = repositories.local()
            .orElseGet(() -> {
                File repository = new File(jqassistantUserDir, "repository");
                repository.mkdirs();
                return repository;
            });
        return localRepository;
    }

    private Optional<org.eclipse.aether.repository.Proxy> getProxy(Optional<Proxy> proxy) {
        return proxy.map(p -> getProxy(p));
    }

    private org.eclipse.aether.repository.Proxy getProxy(Proxy proxy) {
        String protocol = proxy.protocol();
        String host = proxy.host();
        Integer port = proxy.port();
        AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        proxy.username()
            .ifPresent(username -> authBuilder.addUsername(username));
        proxy.password()
            .ifPresent(password -> authBuilder.addPassword(password));

        return new org.eclipse.aether.repository.Proxy(protocol, host, port, authBuilder.build());
    }

    /**
     * Determines the remote repositories to use, using Maven Central as fallback.
     *
     * @param repositories
     *     The {@link Repositories} configuration.
     * @param optionalProxy
     * @return The list of configured {@link RemoteRepository}s.
     */
    private List<RemoteRepository> getRemoteRepositories(Repositories repositories, Optional<org.eclipse.aether.repository.Proxy> optionalProxy) {
        Map<String, Remote> remotes = repositories.remotes();
        if (remotes.isEmpty()) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(CENTRAL_ID, REPOSITORY_LAYOUT_DEFAULT, CENTRAL_URL);
            optionalProxy.ifPresent(proxy -> builder.setProxy(proxy));
            return singletonList(builder.build());
        }
        return remotes.entrySet()
            .stream()
            .map(remoteEntry -> {
                String id = remoteEntry.getKey();
                Remote remote = remoteEntry.getValue();
                AuthenticationBuilder authBuilder = new AuthenticationBuilder();
                remote.username()
                    .ifPresent(username -> authBuilder.addUsername(username));
                remote.password()
                    .ifPresent(password -> authBuilder.addPassword(password));
                RemoteRepository.Builder builder = new RemoteRepository.Builder(id, REPOSITORY_LAYOUT_DEFAULT, remote.url()).setAuthentication(
                        authBuilder.build())
                    .setSnapshotPolicy(SNAPSHOT_REPOSITORY_POLICY);
                optionalProxy.ifPresent(proxy -> builder.setProxy(proxy));
                return builder.build();
            })
            .collect(toList());
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
    private RepositorySystemSession newRepositorySystemSession(Repositories repositories, RepositorySystem system, File localDirectory) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setTransferListener(new TransferListener());
        LocalRepository localRepo = new LocalRepository(localDirectory);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        addMirrors(repositories, session);
        return session;
    }

    private static void addMirrors(Repositories repositories, DefaultRepositorySystemSession session) {
        if (!repositories.mirrors()
            .isEmpty()) {
            DefaultMirrorSelector mirrorSelector = new DefaultMirrorSelector();
            for (Map.Entry<String, Mirror> entry : repositories.mirrors()
                .entrySet()) {
                String id = entry.getKey();
                Mirror mirror = entry.getValue();
                mirrorSelector.add(id, mirror.url(), null, false, mirror.mirrorOf(), null);
            }
            session.setMirrorSelector(mirrorSelector);
        }
    }

    /**
     * A transfer listener logging transfer events.
     */
    private static class TransferListener extends AbstractTransferListener {
        @Override
        public void transferStarted(TransferEvent transferEvent) {
            log.info("Downloading '{}{}'.", transferEvent.getResource()
                .getRepositoryUrl(), transferEvent.getResource()
                .getResourceName());
        }

        @Override
        public void transferSucceeded(TransferEvent transferEvent) {
            log.info("Finished download of '{}{}'.", transferEvent.getResource()
                .getRepositoryUrl(), transferEvent.getResource()
                .getResourceName());
        }
    }
}
