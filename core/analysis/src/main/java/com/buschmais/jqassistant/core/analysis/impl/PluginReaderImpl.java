package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.PluginReader;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.RulesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Plugin reader implementation.
 */
public class PluginReaderImpl implements PluginReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginReaderImpl.class);

    private JAXBContext jaxbContext;

    /**
     * Constructor.
     */
    public PluginReaderImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from the current classpath.
     *
     * @return The plugins which can be resolved from the current classpath.
     */
    @Override
    public List<JqassistantPlugin> readPlugins() {
        final Enumeration<URL> resources;
        try {
            resources = PluginReaderImpl.class.getClassLoader().getResources(PLUGIN_RESOURCE);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get plugin resources.", e);
        }
        List<JqassistantPlugin> plugins = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            LOGGER.info("Reading plugin descriptor from URL '{}'.", url);
            plugins.add(readPlugin(url));
        }
        return plugins;
    }

    /**
     * Read the catalogs from an {@link URL}.
     *
     * @param pluginUrl The {@link URL}.
     * @return The {@link JqassistantPlugin}.
     */
    private JqassistantPlugin readPlugin(URL pluginUrl) {
        InputStream inputStream;
        try {
            inputStream = pluginUrl.openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open plugin stream.", e);
        }
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(JaxbHelper.getSchema(PLUGIN_SCHEMA_RESOURCE));
            return unmarshaller.unmarshal(new StreamSource(inputStream), JqassistantPlugin.class).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read plugin from URL " + pluginUrl.toString(), e);
        }
    }

    @Override
    public List<Source> getRuleSources(Iterable<JqassistantPlugin> plugins) {
        List<Source> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            RulesType rulesType = plugin.getRules();
            String directory = rulesType.getDirectory();
            for (ResourcesType resourcesType : rulesType.getResources()) {
                for (String resource : resourcesType.getResource()) {
                    StringBuffer fullResource = new StringBuffer();
                    if (directory != null) {
                        fullResource.append(directory);
                    }
                    fullResource.append(resource);
                    URL url = PluginReaderImpl.class.getResource(fullResource.toString());
                    String systemId = null;
                    if (url != null) {
                        try {
                            systemId = url.toURI().toString();
                            LOGGER.debug("Adding rulesType from " + url.toString());
                            InputStream ruleStream = url.openStream();
                            sources.add(new StreamSource(ruleStream, systemId));
                        } catch (IOException e) {
                            throw new IllegalStateException("Cannot open rule URL: " + url.toString(), e);
                        } catch (URISyntaxException e) {
                            throw new IllegalStateException("Cannot create URI from url: " + url.toString());
                        }
                    } else {
                        LOGGER.warn("Cannot read rulesType from resource '{}'" + resource);
                    }
                }
            }
        }
        return sources;
    }
}