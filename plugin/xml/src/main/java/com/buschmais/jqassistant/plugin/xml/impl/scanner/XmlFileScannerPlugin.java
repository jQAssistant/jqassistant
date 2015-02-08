package com.buschmais.jqassistant.plugin.xml.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.*;
import com.google.common.base.Strings;

public class XmlFileScannerPlugin extends AbstractScannerPlugin<FileResource, XmlFileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        String lowerCase = path.toLowerCase();
        return lowerCase.endsWith(".xml") || lowerCase.endsWith(".xsd");
    }

    @Override
    public XmlFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = scanner.getContext().getStore();
        XmlElementDescriptor parentElement = null;
        XmlFileDescriptor documentDescriptor = null;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try (InputStream stream = item.createStream()) {
            XMLEventReader eventReader = inputFactory.createXMLEventReader(stream);
            while (eventReader.hasNext()) {
                XMLEvent xmlEvent = eventReader.nextEvent();
                if (xmlEvent.isStartDocument()) {
                    documentDescriptor = store.create(XmlFileDescriptor.class);
                } else if (xmlEvent.isStartElement()) {
                    XmlElementDescriptor elementDescriptor = store.create(XmlElementDescriptor.class);
                    StartElement startElement = xmlEvent.asStartElement();
                    setName(elementDescriptor, startElement.getName());
                    Iterator attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = (Attribute) attributes.next();
                        XmlAttributeDescriptor attributeDescriptor = store.create(XmlAttributeDescriptor.class);
                        setName(attributeDescriptor, attribute.getName());
                        attributeDescriptor.setValue(attribute.getValue());
                        elementDescriptor.getAttributes().add(attributeDescriptor);
                    }
                    if (parentElement == null) {
                        documentDescriptor.setRootElement(elementDescriptor);
                    } else {
                        parentElement.getElements().add(elementDescriptor);
                    }
                    parentElement = elementDescriptor;
                } else if (xmlEvent.isEndElement()) {
                    parentElement = parentElement.getParent();
                } else if (xmlEvent.isCharacters()) {
                    Characters characters = xmlEvent.asCharacters();
                    if (!characters.isWhiteSpace()) {
                        XmlCharactersDescriptor charactersDescriptor = store.create(XmlCharactersDescriptor.class);
                        charactersDescriptor.setData(characters.getData());
                        charactersDescriptor.setCData(characters.isCData());
                        parentElement.getCharacters().add(charactersDescriptor);
                    }
                }
            }
            return documentDescriptor;
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read document.", e);
        }
    }

    /**
     * Set name and namespace related attributes for the given descriptor.
     * 
     * @param descriptor
     *            The descriptor.
     * @param name
     *            The {@link javax.xml.namespace.QName}.
     */
    private void setName(NamespaceDescriptor descriptor, QName name) {
        descriptor.setName(name.getLocalPart());
        String namespaceURI = name.getNamespaceURI();
        if (!Strings.isNullOrEmpty(namespaceURI)) {
            descriptor.setNamespaceUri(namespaceURI);
        }
        String namespacePrefix = name.getPrefix();
        if (!Strings.isNullOrEmpty(namespacePrefix)) {
            descriptor.setNamespacePrefix(namespacePrefix);
        }
    }
}
