package com.buschmais.jqassistant.scm.maven.provider;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginResolverImpl;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

@Component(role = PluginRepositoryProvider.class, instantiationStrategy = "singleton")
public class PluginRepositoryProvider implements Disposable {

    private PluginRepository pluginRepository;

    @Override
    public void dispose() {
        if (this.pluginRepository != null) {
            this.pluginRepository.destroy();
        }
    }

    public PluginRepository getPluginRepository(MavenConfiguration configuration, ArtifactProvider artifactProvider) {
        if (pluginRepository == null) {
            PluginResolver pluginResolver = new PluginResolverImpl(artifactProvider);
            PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(Thread.currentThread().getContextClassLoader(), configuration);

            // do a lazy init of the plugin repo to speed-up if the plugin execution shall be skipped
            PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
            this.pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
            this.pluginRepository.initialize();
        }
        return pluginRepository;
    }

}
