package com.buschmais.jqassistant.plugin.xml.api.scanner;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

public class JAXBUnmarshaller<I extends FileResource, X> {

    private Class<X> rootElementType;

    private XMLInputFactory inputFactory;

    private JAXBContext jaxbContext;

    public JAXBUnmarshaller(Class<X> rootElementType) {
        this.rootElementType = rootElementType;
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try {
            jaxbContext = JAXBContext.newInstance(rootElementType);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context for " + rootElementType.getName(), e);
        }
    }

    public X unmarshal(I item) throws IOException {
        JAXBElement<X> rootElement;
        try (InputStream stream = item.createStream()) {
            XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(stream);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            rootElement = unmarshaller.unmarshal(xmlEventReader, rootElementType);
        } catch (JAXBException e) {
            throw new IOException("Cannot read XML document.", e);
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read XML document.", e);
        }
        return rootElement.getValue();
    }
}
