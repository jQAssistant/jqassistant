package com.buschmais.jqassistant.plugin.xml.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.*;
import com.google.common.base.Strings;

public class XmlFileScannerPlugin extends AbstractScannerPlugin<FileResource, XmlFileDescriptor> {

    private XMLInputFactory inputFactory = XMLInputFactory.newInstance();

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
        Map<String, XmlNamespaceDescriptor> namespaceMappings = new HashMap<>();
        try (InputStream stream = item.createStream()) {
            XMLEventReader eventReader = inputFactory.createXMLEventReader(stream);
            while (eventReader.hasNext()) {
                XMLEvent xmlEvent = eventReader.nextEvent();
                if (xmlEvent.isStartDocument()) {
                    documentDescriptor = store.create(XmlFileDescriptor.class);
                } else {
                    if (xmlEvent.isStartElement()) {
                        XmlElementDescriptor elementDescriptor = store.create(XmlElementDescriptor.class);
                        StartElement startElement = xmlEvent.asStartElement();
                        // get declared namespaces
                        Iterator namespaces = startElement.getNamespaces();
                        while (namespaces.hasNext()) {
                            Namespace namespace = (Namespace) namespaces.next();
                            XmlNamespaceDescriptor namespaceDescriptor = store.create(XmlNamespaceDescriptor.class);
                            String prefix = namespace.getPrefix();
                            if (!Strings.isNullOrEmpty(prefix)) {
                                namespaceDescriptor.setPrefix(prefix);
                                namespaceMappings.put(prefix, namespaceDescriptor);
                            }
                            namespaceDescriptor.setUri(namespace.getNamespaceURI());
                            elementDescriptor.getDeclaredNamespaces().add(namespaceDescriptor);
                        }
                        // set name of element
                        setName(elementDescriptor, startElement.getName(), namespaceMappings);
                        // get attributes
                        Iterator attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = (Attribute) attributes.next();
                            XmlAttributeDescriptor attributeDescriptor = store.create(XmlAttributeDescriptor.class);
                            setName(attributeDescriptor, attribute.getName(), namespaceMappings);
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
                        Iterator namespaces = xmlEvent.asEndElement().getNamespaces();
                        while (namespaces.hasNext()) {
                            Namespace namespace = (Namespace) namespaces.next();
                            String prefix = namespace.getPrefix();
                            if (prefix != null) {
                                namespaceMappings.remove(prefix);
                            }
                        }
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
            }
            return documentDescriptor;
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read document.", e);
        }
    }

    /**
     * Set name and namespace related attributes for the given ofNamespaceDescriptor.
     * 
     * @param ofNamespaceDescriptor
     *            The ofNamespaceDescriptor.
     * @param name
     *            The {@link javax.xml.namespace.QName}.
     */
    private void setName(OfNamespaceDescriptor ofNamespaceDescriptor, QName name, Map<String, XmlNamespaceDescriptor> namespaceMappings) {
        ofNamespaceDescriptor.setName(name.getLocalPart());
        String namespacePrefix = name.getPrefix();
        if (!Strings.isNullOrEmpty(namespacePrefix)) {
            XmlNamespaceDescriptor namespaceDescriptor = namespaceMappings.get(namespacePrefix);
            ofNamespaceDescriptor.setNamespaceDeclaration(namespaceDescriptor);
        }
    }
}
