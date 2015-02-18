package com.buschmais.jqassistant.plugin.xml.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.model.*;
import com.google.common.base.Strings;

public class XmlFileScannerPlugin extends AbstractScannerPlugin<FileResource, XmlFileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlFileScannerPlugin.class);

    private XMLInputFactory inputFactory;

    @Override
    protected void initialize() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

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
        Map<XmlElementDescriptor, SiblingDescriptor> siblings = new HashMap<>();
        try (InputStream stream = item.createStream()) {
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(stream);
            while (streamReader.hasNext()) {
                int eventType = streamReader.getEventType();
                switch (eventType) {
                case XMLStreamConstants.START_DOCUMENT:
                    documentDescriptor = startDocument(streamReader, store);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    XmlElementDescriptor childElement = startElement(streamReader, documentDescriptor, parentElement, namespaceMappings, store);
                    addSibling(parentElement, childElement, siblings);
                    parentElement = childElement;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    parentElement.setLastChild((XmlDescriptor) siblings.remove(parentElement));
                    parentElement = endElement(streamReader, parentElement, namespaceMappings);
                    break;
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CHARACTERS:
                    XmlTextDescriptor textDescriptor = characters(streamReader, XmlTextDescriptor.class, parentElement, store);
                    addSibling(parentElement, textDescriptor, siblings);
                    break;
                case XMLStreamConstants.CDATA:
                    XmlCDataDescriptor cDataDescriptor = characters(streamReader, XmlCDataDescriptor.class, parentElement, store);
                    addSibling(parentElement, cDataDescriptor, siblings);
                    break;
                }
                streamReader.next();
            }
            documentDescriptor.setWellFormed(true);
        } catch (XMLStreamException e) {
            LOGGER.warn("Cannot parse document '" + path + "': " + e.getMessage());
            documentDescriptor.setWellFormed(false);
        }
        return documentDescriptor;
    }

    private <X extends SiblingDescriptor & XmlDescriptor> void addSibling(XmlElementDescriptor parentElement, X child,
            Map<XmlElementDescriptor, SiblingDescriptor> siblings) {
        if (child != null) {
            SiblingDescriptor lastSibling = siblings.get(parentElement);
            if (lastSibling == null) {
                if (parentElement != null) {
                    parentElement.setFirstChild(child);
                }
            } else {
                lastSibling.setNextSibling(child);
            }
            siblings.put(parentElement, child);
        }
    }

    private XmlFileDescriptor startDocument(XMLStreamReader streamReader, Store store) {
        XmlFileDescriptor documentDescriptor;
        documentDescriptor = store.create(XmlFileDescriptor.class);
        documentDescriptor.setVersion(streamReader.getVersion());
        documentDescriptor.setCharacterEncodingScheme(streamReader.getCharacterEncodingScheme());
        documentDescriptor.setStandalone(streamReader.isStandalone());
        return documentDescriptor;
    }

    private XmlElementDescriptor startElement(XMLStreamReader streamReader, XmlFileDescriptor documentDescriptor, XmlElementDescriptor parentElement,
            Map<String, XmlNamespaceDescriptor> namespaceMappings, Store store) {
        XmlElementDescriptor elementDescriptor = store.create(XmlElementDescriptor.class);
        // get namespace declaration
        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            XmlNamespaceDescriptor namespaceDescriptor = store.create(XmlNamespaceDescriptor.class);
            String prefix = streamReader.getNamespacePrefix(i);
            String uri = streamReader.getNamespaceURI(i);
            if (!Strings.isNullOrEmpty(prefix)) {
                namespaceDescriptor.setPrefix(prefix);
                namespaceMappings.put(prefix, namespaceDescriptor);
            }
            namespaceDescriptor.setUri(uri);
            elementDescriptor.getDeclaredNamespaces().add(namespaceDescriptor);
        }
        setName(elementDescriptor, streamReader.getLocalName(), streamReader.getPrefix(), namespaceMappings);

        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            XmlAttributeDescriptor attributeDescriptor = store.create(XmlAttributeDescriptor.class);
            setName(attributeDescriptor, streamReader.getAttributeLocalName(i), streamReader.getAttributePrefix(i), namespaceMappings);
            attributeDescriptor.setValue(streamReader.getAttributeValue(i));
            elementDescriptor.getAttributes().add(attributeDescriptor);
        }

        if (parentElement == null) {
            documentDescriptor.setRootElement(elementDescriptor);
        } else {
            parentElement.getElements().add(elementDescriptor);
        }
        parentElement = elementDescriptor;
        return parentElement;
    }

    private XmlElementDescriptor endElement(XMLStreamReader streamReader, XmlElementDescriptor parentElement,
            Map<String, XmlNamespaceDescriptor> namespaceMappings) {
        parentElement = parentElement.getParent();
        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            String prefix = streamReader.getNamespacePrefix(i);
            if (!Strings.isNullOrEmpty(prefix)) {
                namespaceMappings.remove(prefix);
            }
        }
        return parentElement;
    }

    private <T extends XmlTextDescriptor> T characters(XMLStreamReader streamReader, Class<T> type, XmlElementDescriptor parentElement, Store store) {
        if (streamReader.hasText()) {
            int start = streamReader.getTextStart();
            int length = streamReader.getTextLength();
            String text = new String(streamReader.getTextCharacters(), start, length).trim();
            if (!Strings.isNullOrEmpty(text)) {
                T textDescriptor = store.create(type);
                textDescriptor.setValue(text);
                parentElement.getCharacters().add(textDescriptor);
                return textDescriptor;
            }
        }
        return null;
    }

    private void setName(OfNamespaceDescriptor ofNamespaceDescriptor, String localName, String prefix, Map<String, XmlNamespaceDescriptor> namespaceMappings) {
        ofNamespaceDescriptor.setName(localName);
        if (!Strings.isNullOrEmpty(prefix)) {
            XmlNamespaceDescriptor namespaceDescriptor = namespaceMappings.get(prefix);
            ofNamespaceDescriptor.setNamespaceDeclaration(namespaceDescriptor);
        }
    }

}
