package com.buschmais.jqassistant.scm.maven.provider;

import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginClassLoader;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginResolver;
import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;
import com.buschmais.jqassistant.core.plugin.impl.AetherPluginResolverImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

@Component(role = PluginRepositoryProvider.class, instantiationStrategy = "singleton")
public class PluginRepositoryProvider implements Disposable {

    private PluginRepository pluginRepository;

    @Override
    public synchronized void dispose() {
        if (this.pluginRepository != null) {
            this.pluginRepository.destroy();
        }
    }

    public synchronized PluginRepository getPluginRepository(RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession,
        List<RemoteRepository> repositories, List<Plugin> plugins) {
        if (pluginRepository == null) {

            PluginResolver pluginResolver = new AetherPluginResolverImpl(repositorySystem, repositorySystemSession, repositories);
            PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(Thread.currentThread().getContextClassLoader(), plugins);

            // do a lazy init of the plugin repo to speed-up if the plugin execution shall be skipped
            PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
            this.pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
            this.pluginRepository.initialize();
        }
        return pluginRepository;
    }

}
