
package com.buschmais.jqassistant.scm.maven.provider;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
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
        try {
            PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
            pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
            this.pluginRepository.initialize();
        } catch (PluginRepositoryException e) {
            LOGGER.warn("Cannot initialize plugin repository.", e);
        }
    }

    @Override
    public synchronized void dispose() {
        if (this.pluginRepository != null) {
            try {
                this.pluginRepository.destroy();
            } catch (PluginRepositoryException e) {
                LOGGER.warn("Cannot destroy plugin repository.", e);
            }
        }
    }

    public PluginRepository getPluginRepository() {
        return pluginRepository;
    }

}
