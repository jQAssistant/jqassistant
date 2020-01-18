package com.buschmais.jqassistant.core.shared.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.validation.Schema;

/**
 * Utility class for unmarshalling XML documents using JAXB.
 * <p>
 * A constructor is provided that takes a target namespace for to be used for
 * unmarshalling (e.g. for reading a persistence.xml 2.0 descriptor using a 2.1
 * JAXBContext).
 *
 * @param <X>
 *            The JAXB type of the root element.
 */
public class JAXBUnmarshaller<X> {

    private Class<X> rootElementType;

    private Schema schema;

    private String targetNamespace;

    private XMLInputFactory inputFactory;

    private JAXBContext jaxbContext;

    /**
     * Constructor.
     *
     * @param rootElementType
     *            The expected root element type.
     */
    public JAXBUnmarshaller(Class<X> rootElementType) {
        this(rootElementType, null, null);
    }

    /**
     * Constructor.
     *
     * @param rootElementType
     *            The expected root element type.
     * @param targetNamespace
     *            The target namespace to use for unmarshalling.
     */
    public JAXBUnmarshaller(Class<X> rootElementType, String targetNamespace) {
        this(rootElementType, null, targetNamespace);
    }

    /**
     * Constructor.
     *
     * @param rootElementType
     *            The expected root element type.
     * @param schema
     *            The optional schema for validation.
     * @param targetNamespace
     *            The target namespace to use for unmarshalling.
     */
    public JAXBUnmarshaller(Class<X> rootElementType, Schema schema, String targetNamespace) {
        this.rootElementType = rootElementType;
        this.schema = schema;
        this.targetNamespace = targetNamespace;
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try {
            jaxbContext = JAXBContext.newInstance(rootElementType);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context for " + rootElementType.getName(), e);
        }
    }

    public X unmarshal(InputStream stream) throws IOException {
        try {
            XMLStreamReader xmlStreamReader = new NamespaceMappingStreamReader(inputFactory.createXMLStreamReader(stream), targetNamespace);
            return unmarshal(xmlStreamReader);
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read XML document.", e);
        }
    }

    private X unmarshal(XMLStreamReader xmlStreamReader) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (schema != null) {
                unmarshaller.setSchema(schema);
            }
            return unmarshaller.unmarshal(xmlStreamReader, rootElementType).getValue();
        } catch (JAXBException e) {
            throw new IOException("Cannot unmarshal XML document.", e);
        }
    }

    /**
     * A {@link StreamReaderDelegate} which maps all namespaces from a document to a
     * specified namespace.
     */
    private static class NamespaceMappingStreamReader extends StreamReaderDelegate {

        private String targetNamespace;

        public NamespaceMappingStreamReader(XMLStreamReader reader, String targetNamespace) {
            super(reader);
            this.targetNamespace = targetNamespace;
        }

        @Override
        public String getNamespaceURI() {
            return map(super.getNamespaceURI());
        }

        private String map(String documentNamespace) {
            return targetNamespace != null ? targetNamespace : documentNamespace;
        }
    }
}
