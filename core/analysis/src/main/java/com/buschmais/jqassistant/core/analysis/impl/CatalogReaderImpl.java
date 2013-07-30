package com.buschmais.jqassistant.core.analysis.impl;

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
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class CatalogReaderImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogReaderImpl.class);

    private JAXBContext jaxbContext;

    public CatalogReaderImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    public Iterable<URL> readCatalogs() {
        final Enumeration<URL> resources;
        try {
            resources = CatalogReaderImpl.class.getClassLoader().getResources("META-INF/jqassistant/jqassistant-catalog.xml");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get catalog resources.", e);
        }
        return new Iterable<URL>() {

            @Override
            public Iterator<URL> iterator() {
                return new Iterator<URL>() {
                    @Override
                    public boolean hasNext() {
                        return resources.hasMoreElements();
                    }

                    @Override
                    public URL next() {
                        return resources.nextElement();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Remove operation is not supported.");
                    }
                };
            }
        };
    }

    public Iterable<URL> readCatalogs(Iterable<URL> urls) {
        List<URL> resourceUrls = new ArrayList<URL>();
        for (URL url : urls) {
            resourceUrls.addAll(readCatalog(url));
        }
        return resourceUrls;
    }

    private List<URL> readCatalog(URL catalogUrl) {
        InputStream inputStream;
        try {
            inputStream = catalogUrl.openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open Catalog stream.", e);
        }
        JqassistantCatalog catalog;
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(JaxbHelper.getSchema("/META-INF/xsd/jqassistant-catalog-1.0.xsd"));
            catalog = unmarshaller.unmarshal(new StreamSource(inputStream), JqassistantCatalog.class).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read catalog from URL " + catalogUrl.toString(), e);
        }
        List<URL> resourceUrls = new ArrayList<URL>();
        for (String resource : catalog.getResource()) {
            URL resourceUrl = CatalogReaderImpl.class.getResource("/META-INF/jqassistant/" + resource);
            if (resourceUrl != null) {
                resourceUrls.add(resourceUrl);
            } else {
                LOGGER.warn("Cannot find resource '{}' referenced from catalog '{}'", resourceUrl, catalogUrl);
            }
        }
        return resourceUrls;
    }
}
