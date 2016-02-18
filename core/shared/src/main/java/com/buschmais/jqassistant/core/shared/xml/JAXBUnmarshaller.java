package com.buschmais.jqassistant.core.shared.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for unmarshalling XML documents using JAXB.
 * <p>
 * A constructor is provided that takes namespace mappings which may be used to
 * unmarshal documents using an older, i.e. compatible schema version (e.g. for
 * reading a persistence.xml 2.0 descriptor using a 2.1 JAXBContext).
 *
 * @param <X> The JAXB type of the root element.
 */
public class JAXBUnmarshaller<X> {

    private Class<X> rootElementType;

    private Map<String, String> namespaceMapping;

    private XMLInputFactory inputFactory;

    private JAXBContext jaxbContext;

    /**
     * Constructor.
     *
     * @param rootElementType The expected root element type.
     */
    public JAXBUnmarshaller(Class<X> rootElementType) {
        this(rootElementType, Collections.<String, String>emptyMap());
    }

    /**
     * Constructor.
     *
     * @param rootElementType  The expected root element type.
     * @param namespaceMapping The namespace mappings. The key namespaces contained in the
     *                         map will be replaced their values while reading documents.
     */
    public JAXBUnmarshaller(Class<X> rootElementType, Map<String, String> namespaceMapping) {
        this.rootElementType = rootElementType;
        this.namespaceMapping = namespaceMapping;
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
            XMLStreamReader xmlStreamReader = new NamespaceMappingStreamReader(inputFactory.createXMLStreamReader(stream), namespaceMapping);
            return unmarshal(xmlStreamReader);
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read XML document.", e);
        }
    }

    private X unmarshal(XMLStreamReader xmlStreamReader) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(xmlStreamReader, rootElementType).getValue();
        } catch (JAXBException e) {
            throw new IOException("Cannot unmarshal XML document.", e);
        }
    }

    /**
     * A {@link StreamReaderDelegate} which maps all namespaces from a document
     * to a specified namespace.
     */
    private static class NamespaceMappingStreamReader extends StreamReaderDelegate {

        private Map<String, String> namespaceMapping;

        public NamespaceMappingStreamReader(XMLStreamReader reader, Map<String, String> namespaceMapping) {
            super(reader);
            this.namespaceMapping = namespaceMapping;
        }

        @Override
        public String getAttributeNamespace(int index) {
            return map(super.getAttributeNamespace(index));
        }

        @Override
        public String getNamespaceURI() {
            return map(super.getNamespaceURI());
        }

        private String map(String documentNamespace) {
            String namespace = namespaceMapping.get(documentNamespace);
            return namespace != null ? namespace : documentNamespace;
        }
    }
}
