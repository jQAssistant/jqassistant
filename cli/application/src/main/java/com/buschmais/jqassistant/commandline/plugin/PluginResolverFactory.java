package com.buschmais.jqassistant.commandline.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.configuration.Proxy;
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
import org.eclipse.aether.repository.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;

import static java.util.Optional.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_FAIL;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

/**
 * Factory for the {@link PluginResolver} to be used in standalone in the CLI.
 */
@Slf4j
public class PluginResolverFactory {

    public static final String MAVEN_CENTRAL_ID = "central";

    public static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";

    private static final RepositoryPolicy SNAPSHOT_REPOSITORY_POLICY = new RepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_FAIL);

    private static final String REPOSITORY_LAYOUT_DEFAULT = "default";

    private static final Remote MAVEN_CENTRAL = new Remote() {
        @Override
        public String url() {
            return MAVEN_CENTRAL_URL;
        }

        @Override
        public Optional<String> username() {
            return empty();
        }

        @Override
        public Optional<String> password() {
            return empty();
        }
    };

    private final File jqassistantUserDir;

    public PluginResolverFactory(File userHome) {
        this.jqassistantUserDir = new File(userHome, ".jqassistant");
    }

    /**
     * Creates a {@link PluginResolver} using the given {@link CliConfiguration} providing the required {@link Plugin}s and {@link Repositories}
     * <p>
     * The default local repository is ~/.jqassistant/repository.
     * <p>
     * If no remote repository is specified the resolver will use Maven Central.
     *
     * @param configuration
     *     The {@link CliConfiguration}.
     * @return The {@link PluginResolver}.
     */
    public PluginResolver create(CliConfiguration configuration) {
        Repositories repositories = configuration.repositories();
        File localRepository = getLocalRepository(repositories);
        Optional<org.eclipse.aether.repository.Proxy> proxy = getProxy(configuration.proxy());
        ProxySelector proxySelector = getProxySelector(configuration, proxy);
        Optional<MirrorSelector> mirrorSelector = getMirrorSelector(repositories);
        List<RemoteRepository> remoteRepositories = getRemoteRepositories(repositories, proxy, mirrorSelector);
        RepositorySystem repositorySystem = newRepositorySystem();
        log.info("Using local repository '{}' and remote repositories {}.", localRepository, remoteRepositories.stream()
            .map(remoteRepository -> "'" + remoteRepository.getId() + " (" + remoteRepository.getUrl() + ")'")
            .collect(joining(", ")));
        RepositorySystemSession session = newRepositorySystemSession(repositorySystem, localRepository, mirrorSelector, proxySelector);

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
        return repositories.local()
            .orElseGet(() -> {
                File repository = new File(jqassistantUserDir, "repository");
                repository.mkdirs();
                return repository;
            });
    }

    private Optional<org.eclipse.aether.repository.Proxy> getProxy(Optional<Proxy> proxy) {
        return proxy.map(p -> {
            String protocol = p.protocol();
            String host = p.host();
            Integer port = p.port();
            AuthenticationBuilder authBuilder = new AuthenticationBuilder();
            p.username()
                .ifPresent(username -> authBuilder.addUsername(username));
            p.password()
                .ifPresent(password -> authBuilder.addPassword(password));

            return new org.eclipse.aether.repository.Proxy(protocol, host, port, authBuilder.build());
        });
    }

    private ProxySelector getProxySelector(CliConfiguration configuration, Optional<org.eclipse.aether.repository.Proxy> proxy) {
        DefaultProxySelector proxySelector = new DefaultProxySelector();
        proxy.ifPresent(p -> proxySelector.add(p, configuration.proxy()
            .map(Proxy::nonProxyHosts)
            .orElse(null)));
        return proxySelector;
    }

    /**
     * Determines the remote repositories to use, using Maven Central as fallback.
     *
     * @param repositories
     *     The {@link Repositories} configuration.
     * @param optionalProxy
     * @param mirrorSelector
     * @return The list of configured {@link RemoteRepository}s.
     */
    private List<RemoteRepository> getRemoteRepositories(Repositories repositories, Optional<org.eclipse.aether.repository.Proxy> optionalProxy,
        Optional<MirrorSelector> mirrorSelector) {
        // Use Maven Central as default
        Map<String, Remote> remotes = new HashMap<>();
        remotes.put(MAVEN_CENTRAL_ID, MAVEN_CENTRAL);
        // Add configured remotes
        remotes.putAll(repositories.remotes());
        return remotes.entrySet()
            .stream()
            .map(remoteEntry -> getRemoteRepository(remoteEntry.getKey(), remoteEntry.getValue(), optionalProxy))
            // apply any configured mirrors to the remote repository
            .map(remoteRepository -> mirrorSelector.map(selector -> ofNullable(selector.getMirror(remoteRepository)).orElse(remoteRepository))
                .orElse(remoteRepository))
            .collect(toList());
    }

    private static RemoteRepository getRemoteRepository(String id, Remote remote, Optional<org.eclipse.aether.repository.Proxy> optionalProxy) {
        AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        remote.username()
            .ifPresent(username -> authBuilder.addUsername(username));
        remote.password()
            .ifPresent(password -> authBuilder.addPassword(password));
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id, REPOSITORY_LAYOUT_DEFAULT, remote.url()).setAuthentication(authBuilder.build())
            .setSnapshotPolicy(SNAPSHOT_REPOSITORY_POLICY);
        optionalProxy.ifPresent(proxy -> builder.setProxy(proxy));
        return builder.build();
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
     * @param mirrorSelector The optional {@link MirrorSelector}.
     * @param proxySelector The {@link ProxySelector}.
     * @return a new {@link RepositorySystemSession}.
     */
    private RepositorySystemSession newRepositorySystemSession(RepositorySystem system, File localDirectory, Optional<MirrorSelector> mirrorSelector,
        ProxySelector proxySelector) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setTransferListener(new TransferListener());
        LocalRepository localRepo = new LocalRepository(localDirectory);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setProxySelector(proxySelector);
        mirrorSelector.ifPresent(selector -> session.setMirrorSelector(selector));
        return session;
    }

    private Optional<MirrorSelector> getMirrorSelector(Repositories repositories) {
        if (repositories.mirrors()
            .isEmpty()) {
            return empty();
        }
        DefaultMirrorSelector mirrorSelector = new DefaultMirrorSelector();
        for (Map.Entry<String, Mirror> entry : repositories.mirrors()
            .entrySet()) {
            String id = entry.getKey();
            Mirror mirror = entry.getValue();
            mirrorSelector.add(id, mirror.url(), null,false, false, mirror.mirrorOf(), null);
        }
        return of(mirrorSelector);
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
