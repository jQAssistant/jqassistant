package com.buschmais.jqassistant.core.pluginmanager.impl;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.RulesType;
import com.buschmais.jqassistant.core.pluginmanager.api.RulePluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule repository implementation.
 */
public class RulePluginManagerImpl extends PluginManagerImpl implements RulePluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulePluginManagerImpl.class);

    private List<Source> sources;

    /**
     * Constructor.
     */
    public RulePluginManagerImpl() throws PluginReaderException {
        this.sources = getRuleSources(getPlugins());
    }

    @Override
    public List<Source> getRuleSources() {
        return sources;
    }

    private List<Source> getRuleSources(List<JqassistantPlugin> plugins) {
        List<Source> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : getPlugins()) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                String directory = rulesType.getDirectory();
                for (ResourcesType resourcesType : rulesType.getResources()) {
                    for (String resource : resourcesType.getResource()) {
                        StringBuffer fullResource = new StringBuffer();
                        if (directory != null) {
                            fullResource.append(directory);
                        }
                        fullResource.append(resource);
                        URL url = RulePluginManagerImpl.class.getResource(fullResource.toString());
                        String systemId = null;
                        if (url != null) {
                            try {
                                systemId = url.toURI().toString();
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Adding rulesType from " + url.toString());
                                }
                                InputStream ruleStream = url.openStream();
                                sources.add(new StreamSource(ruleStream, systemId));
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot open rules URL: " + url.toString(), e);
                            } catch (URISyntaxException e) {
                                throw new IllegalStateException("Cannot create URI from url: " + url.toString());
                            }
                        } else {
                            LOGGER.warn("Cannot read rulesType from resource '{}'" + resource);
                        }
                    }
                }
            }
        }
        return sources;
    }
}
