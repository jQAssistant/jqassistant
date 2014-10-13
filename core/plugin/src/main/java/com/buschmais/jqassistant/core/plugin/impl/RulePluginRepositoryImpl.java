package com.buschmais.jqassistant.core.plugin.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RulesType;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl implements RulePluginRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulePluginRepositoryImpl.class);

    private List<RuleSource> sources;

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        this.sources = getRuleSources(pluginConfigurationReader.getPlugins());
    }

    @Override
    public List<RuleSource> getRuleSources() {
        return sources;
    }

    private List<RuleSource> getRuleSources(List<JqassistantPlugin> plugins) {
        List<RuleSource> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                String directory = rulesType.getDirectory();
                for (String resource : rulesType.getResource()) {
                    StringBuilder fullResource = new StringBuilder();
                    if (directory != null) {
                        fullResource.append(directory);
                    }
                    fullResource.append(resource);
                    URL url = RulePluginRepositoryImpl.class.getResource(fullResource.toString());
                    if (url != null) {
                        sources.add(new RuleSource(url));
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Adding rulesType from " + url.toString());
                        }
                    } else {
                        LOGGER.warn("Cannot read rules from resource '{}'", fullResource);
                    }
                }
            }
        }
        return sources;
    }
}
