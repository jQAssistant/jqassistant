package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.JqassistantCatalog;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
    public Iterable<JqassistantCatalog> readCatalogs() {
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
        return catalogs;
    }

    /**
     * Reads the catalogs from the {@link URL}s provided by the given {@link Iterable}.
     *
     * @param urls The {@link URL}s.
     * @return The {@link JqassistantCatalog}s.
     */
    private List<JqassistantCatalog> readCatalogs(Iterable<URL> urls) {
        List<JqassistantCatalog> catalogs = new ArrayList<JqassistantCatalog>();
        for (URL url : urls) {
            catalogs.add(readCatalog(url));
        }
        return catalogs;
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
}