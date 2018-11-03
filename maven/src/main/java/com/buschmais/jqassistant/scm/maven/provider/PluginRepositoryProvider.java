
package com.buschmais.jqassistant.scm.maven.provider;

import javax.inject.Singleton;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PluginRepositoryProvider implements Initializable, Disposable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepositoryProvider.class);

    private PluginRepository pluginRepository;

    @Override
    public synchronized void initialize() {
        if (pluginRepository == null) {
            try {
                PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
                pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
                this.pluginRepository.initialize();
            } catch (PluginRepositoryException e) {
                LOGGER.warn("Cannot initialize plugin repository.", e);
            }
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
