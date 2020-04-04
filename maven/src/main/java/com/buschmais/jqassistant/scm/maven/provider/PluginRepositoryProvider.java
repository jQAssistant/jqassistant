
package com.buschmais.jqassistant.scm.maven.provider;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

@Component(role = PluginRepositoryProvider.class, instantiationStrategy = "singleton")
public class PluginRepositoryProvider implements Disposable {

    private PluginRepository pluginRepository;

    public PluginRepositoryProvider() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        this.pluginRepository.initialize();
    }

    @Override
    public synchronized void dispose() {
        if (this.pluginRepository != null) {
            this.pluginRepository.destroy();
        }
    }

    public PluginRepository getPluginRepository() {
        return pluginRepository;
    }

}
