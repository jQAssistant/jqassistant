package com.buschmais.jqassistant.core.plugin.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

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

    private List<URL> ruleUrls;

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        this.ruleUrls = getRuleUrls(pluginConfigurationReader.getPlugins());
    }

    @Override
    public List<Source> getRuleSources() {
        return getRuleSources(ruleUrls);
    }

    /**
     * Get the sources for the given URLs.
     * 
     * @param urls
     *            the URLs for which to get the sources
     * @return the list of sources
     */
    private List<Source> getRuleSources(List<URL> urls) {
        List<Source> someSources = new ArrayList<>();
        for (URL url : urls) {
            String systemId;
            try {
                systemId = url.toURI().toString();
                InputStream ruleStream = url.openStream();
                someSources.add(new StreamSource(ruleStream, systemId));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot open rules URL: " + url.toString(), e);
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Cannot create URI from url: " + url.toString());
            }
        }
        return someSources;
    }

    /**
     * Get the URLs of the rules for the given plugins.
     * 
     * @param plugins
     *            the plugins for which to get the URLs
     * @return the list of URLs
     */
    private List<URL> getRuleUrls(List<JqassistantPlugin> plugins) {

        List<URL> urls = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                String directory = rulesType.getDirectory();
                for (String resource : rulesType.getResource()) {
                    StringBuffer fullResource = new StringBuffer();
                    if (directory != null) {
                        fullResource.append(directory);
                    }
                    fullResource.append(resource);
                    URL url = RulePluginRepositoryImpl.class.getResource(fullResource.toString());
                    if (url != null) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Adding rulesType from " + url.toString());
                        }urls.add(url);
                    } else {
                        LOGGER.warn("Cannot read rules from resource '{}'", fullResource);
                    }
                }
            }
        }
        return urls;
    }
}
