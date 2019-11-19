
package com.buschmais.jqassistant.scm.maven.provider;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = PluginRepositoryProvider.class, instantiationStrategy = "singleton")
public class PluginRepositoryProvider implements Disposable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepositoryProvider.class);

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
