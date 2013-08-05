package com.buschmais.jqassistant.core.analysis.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.RulesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.JqassistantCatalog;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.ObjectFactory;

/**
 * Catalog reader implementation.
 */
public class CatalogReaderImpl implements CatalogReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogReaderImpl.class);

    private JAXBContext jaxbContext;

    /**
     * Constructor.
     */
    public CatalogReaderImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    /**
     * Returns an {@link Iterable} over all catalogs which can be resolved from the current classpath.
     *
     * @return The catalogs which can be resolved from the current classpath.
     */
    @Override
    public List<Source> readCatalogs() {
        final Enumeration<URL> resources;
        try {
            resources = CatalogReaderImpl.class.getClassLoader().getResources(CATALOG_RESOURCE);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get catalog resources.", e);
        }
        List<JqassistantCatalog> catalogs = new ArrayList<JqassistantCatalog>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            LOGGER.debug("Reading catalog from URL '{}'.", url);
            catalogs.add(readCatalog(url));
        }
        return convert(catalogs);
    }

    /**
     * Read the catalogs from an {@link URL}.
     *
     * @param catalogUrl The {@link URL}.
     * @return The {@link JqassistantCatalog}.
     */
    private JqassistantCatalog readCatalog(URL catalogUrl) {
        InputStream inputStream;
        try {
            inputStream = catalogUrl.openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open Catalog stream.", e);
        }
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(JaxbHelper.getSchema(CATALOG_SCHEMA_RESOURCE));
            return unmarshaller.unmarshal(new StreamSource(inputStream), JqassistantCatalog.class).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read catalog from URL " + catalogUrl.toString(), e);
        }
    }

    public List<Source> convert(Iterable<JqassistantCatalog> catalogs) {
        List<Source> sources = new ArrayList<Source>();
        for (JqassistantCatalog catalog : catalogs) {
            for (RulesType rulesType : catalog.getRules()) {
                for (ResourcesType resourcesType : rulesType.getResources()) {
                    String directory = resourcesType.getDirectory();
                    for (String resource : resourcesType.getResource()) {
                        StringBuffer fullResource = new StringBuffer();
                        if (directory != null) {
                            fullResource.append(directory);
                        }
                        fullResource.append(resource);
                        URL url = CatalogReaderImpl.class.getResource(fullResource.toString());
                        String systemId = null;
                        if (url != null) {
                            try {
                                systemId = url.toURI().toString();
                                LOGGER.debug("Adding rules from " + url.toString());
                                InputStream ruleStream = url.openStream();
                                sources.add(new StreamSource(ruleStream, systemId));
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot open rule URL: " + url.toString(), e);
                            } catch (URISyntaxException e) {
                                throw new IllegalStateException("Cannot create URI from url: " + url.toString());
                            }
                        } else {
                            LOGGER.warn("Cannot read rules from resource '{}'" + resource);
                        }
                    }
                }
            }
        }
        return sources;
    }
}