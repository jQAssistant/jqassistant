package com.buschmais.jqassistant.core.shared.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

/**
 * Provides utility functions for working with XML files.
 */
@Slf4j
public class XmlHelper {

    private final static XMLInputFactory xmlInputFactory;

    static {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    public static XMLInputFactory getXMLInputFactory() {
        return xmlInputFactory;
    }

    public static boolean rootElementMatches(InputStreamSupplier inputStreamSupplier, Predicate<QName> rootElementPredicate) {
        try (InputStream stream = inputStreamSupplier.get()) {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(stream);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    return rootElementPredicate.test(reader.getName());
                }
            }
        } catch (XMLStreamException e) {
            log.warn("Cannot parse XML file.");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot close XML document.", e);
        }
        return false;
    }

    public interface InputStreamSupplier {

        InputStream get() throws IOException;

    }

    /**
     * Return a {@link Schema} instance for the given resource.
     *
     * @param resource
     *            The resource.
     * @return The {@link Schema} instance.
     */
    public static Schema getSchema(String resource) {
        Schema schema;
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(XmlHelper.class.getResource(resource));
        } catch (SAXException e) {
            throw new IllegalStateException("Cannot read rules schema.", e);
        }
        return schema;
    }
}
