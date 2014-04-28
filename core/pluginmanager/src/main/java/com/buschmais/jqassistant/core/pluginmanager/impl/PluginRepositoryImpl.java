package com.buschmais.jqassistant.core.pluginmanager.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.impl.XmlHelper;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.pluginmanager.api.PluginRepository;

/**
 * Plugin reader implementation.
 */
public class PluginRepositoryImpl implements PluginRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepositoryImpl.class);

    private JAXBContext jaxbContext;

    private List<JqassistantPlugin> plugins = null;

    /**
     * Constructor.
     */
    public PluginRepositoryImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    /**
     * Read the catalogs from an {@link URL}.
     * 
     * @param pluginUrl
     *            The {@link URL}.
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
            unmarshaller.setSchema(XmlHelper.getSchema(PLUGIN_SCHEMA_RESOURCE));
            return unmarshaller.unmarshal(new StreamSource(inputStream), JqassistantPlugin.class).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read plugin from " + pluginUrl.toString(), e);
        }
    }

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from
     * the current classpath.
     * 
     * @return The plugins which can be resolved from the current classpath.
     */
    protected List<JqassistantPlugin> getPlugins() {
        if (this.plugins == null) {
            final Enumeration<URL> resources;
            try {
                resources = PluginRepositoryImpl.class.getClassLoader().getResources(PLUGIN_RESOURCE);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot get plugin resources.", e);
            }
            this.plugins = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Reading plugin descriptor from URL '{}'.", url);
                }
                this.plugins.add(readPlugin(url));
            }
        }
        return this.plugins;
    }
}
