package com.buschmais.jqassistant.plugin.xml.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests the generic XML scanner.
 */
class XmlFileScannerIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    /**
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void validXmlSource() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/validDocument.xml");
        Source source = new StreamSource(xmlFile);
        Scanner scanner = getScanner();
        XmlDocumentDescriptor documentDescriptor = store.create(XmlDocumentDescriptor.class);
        scanner.getContext().push(XmlDocumentDescriptor.class, documentDescriptor);
        scanner.scan(source, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        scanner.getContext().pop(XmlDocumentDescriptor.class);
        verifyDocument(documentDescriptor);
        store.commitTransaction();
    }

    /**
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void validXmlFile() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/validDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        verifyDocument(xmlFileDescriptor);
        store.commitTransaction();
    }

    @Test
    void undefinedNameSpacesXmlFile() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/undefinedNamespaces.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        //verifyDocument(xmlFileDescriptor);
        store.commitTransaction();
    }

    @Test
    void excludeXmlFile() {
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/validDocument.xml");
        FileDescriptor fileDescriptor = getScanner(Map.of("xml.file.exclude", "*.xml")).scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(fileDescriptor).isNotInstanceOf(XmlFileDescriptor.class);
    }

    private void verifyDocument(XmlDocumentDescriptor xmlDocumentDescriptor) {
        assertThat(xmlDocumentDescriptor).isNotNull();
        assertThat(xmlDocumentDescriptor.isXmlWellFormed()).isEqualTo(true);
        assertThat(xmlDocumentDescriptor.getXmlVersion()).isEqualTo("1.0");
        assertThat(xmlDocumentDescriptor.getCharacterEncodingScheme()).isEqualTo("UTF-8");
        assertThat(xmlDocumentDescriptor.isStandalone()).isEqualTo(false);
        assertThat(xmlDocumentDescriptor.getLineNumber()).isEqualTo(1);
        XmlElementDescriptor rootElement = xmlDocumentDescriptor.getRootElement();
        assertThat(rootElement).isNotNull();
        assertThat(rootElement.getName()).isEqualTo("RootElement");
        assertThat(rootElement.getLineNumber()).isEqualTo(2);
        List<XmlNamespaceDescriptor> rootNamespaces = rootElement.getDeclaredNamespaces();
        assertThat(rootNamespaces.size()).isEqualTo(1);
        XmlNamespaceDescriptor rootNS = rootNamespaces.get(0);
        assertThat(rootNS.getUri()).isEqualTo("http://jqassistant.org/plugin/xml/test/root");
        assertThat(rootNS.getPrefix()).isNull();
        List<XmlElementDescriptor> childElements = rootElement.getElements();
        assertThat(childElements.size()).isEqualTo(3);
        for (XmlElementDescriptor childElement : childElements) {
            if ("ChildElement".equals(childElement.getName())) {
                verifyChildElement(childElement);
            } else if ("ExtraElement".equals(childElement.getName())) {
                verifyExtraElement(childElement);
            } else if ("MixedParentElement".equals(childElement.getName())) {
                verifyMixedParentElement(childElement);
            } else {
                fail("Found unexpected child element: " + childElement.getName());
            }
        }
    }

    private void verifyChildElement(XmlElementDescriptor childElement) {
        assertThat(childElement.getDeclaredNamespaces().size()).isEqualTo(0);
        assertThat(childElement.getLineNumber()).isEqualTo(3);
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size()).isEqualTo(1);
        XmlAttributeDescriptor childElementAttribute = childElementAttributes.get(0);
        assertThat(childElementAttribute.getName()).isEqualTo("attribute1");
        List<XmlTextDescriptor> childElementTexts = childElement.getCharacters();
        assertThat(childElementTexts.size()).isEqualTo(1); // non-coalescing StAX reports multiple text events, ensure it's activated
        XmlTextDescriptor childElementText = childElementTexts.get(0);
        assertThat(childElementText.getValue()).isEqualTo("Child\nText\nOn\nMultiple\nLines");
        assertThat(childElementText.getLineNumber()).isEqualTo(9);
    }

    private void verifyExtraElement(XmlElementDescriptor childElement) {
        assertThat(childElement.getDeclaredNamespaces().size()).isEqualTo(1);
        assertThat(childElement.getLineNumber()).isEqualTo(10);
        XmlNamespaceDescriptor extraNamespace = childElement.getDeclaredNamespaces().get(0);
        assertThat(extraNamespace.getUri()).isEqualTo("http://jqassistant.org/plugin/xml/test/extra");
        assertThat(extraNamespace.getPrefix()).isEqualTo("extra");
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size()).isEqualTo(0);
        List<XmlElementDescriptor> extraChildElements = childElement.getElements();
        assertThat(extraChildElements.size()).isEqualTo(1);
        XmlElementDescriptor extraChildElement = extraChildElements.get(0);
        assertThat(extraChildElement.getName()).isEqualTo("ExtraChildElement");
        assertThat(extraChildElement.getLineNumber()).isEqualTo(11);
        assertThat(extraChildElement.getNamespaceDeclaration()).isEqualTo(extraNamespace);
        List<XmlTextDescriptor> extraChildElementTexts = extraChildElement.getCharacters();
        assertThat(extraChildElementTexts.size()).isEqualTo(1);
        XmlTextDescriptor extraChildElementText = extraChildElementTexts.get(0);
        assertThat(extraChildElementText.getValue()).isEqualTo("Extra\nChild\nText\nOn\nMultiple\nLines");
        assertThat(extraChildElementText.getLineNumber()).isEqualTo(17);
        List<XmlAttributeDescriptor> extraChildElementAttributes = extraChildElement.getAttributes();
        assertThat(extraChildElementAttributes.size()).isEqualTo(1);
        XmlAttributeDescriptor extraChildElementAttribute = extraChildElementAttributes.get(0);
        assertThat(extraChildElementAttribute.getName()).isEqualTo("attribute2");
        assertThat(extraChildElementAttribute.getNamespaceDeclaration()).isEqualTo(extraNamespace);
    }

    private void verifyMixedParentElement(XmlElementDescriptor childElement) {
        assertThat(childElement.getLineNumber()).isEqualTo(19);
        XmlDescriptor mixedChildElement1 = childElement.getFirstChild();
        assertThat(mixedChildElement1).isNotNull();
        assertThat(mixedChildElement1).isInstanceOf(XmlElementDescriptor.class);
        assertThat(((XmlElementDescriptor) mixedChildElement1).getName()).isEqualTo("MixedChildElement1");
        assertThat(((XmlElementDescriptor) mixedChildElement1).getLineNumber()).isEqualTo(20);
        XmlDescriptor mixedChildText = ((XmlElementDescriptor) mixedChildElement1).getNextSibling();
        assertThat(mixedChildText).isNotNull();
        assertThat(mixedChildText).isInstanceOf(XmlTextDescriptor.class);
        assertThat(((XmlTextDescriptor) mixedChildText).getValue()).isEqualTo("Mixed Parent Text");
        assertThat(((XmlTextDescriptor) mixedChildText).getLineNumber()).isEqualTo(22);
        XmlDescriptor mixedChildElement2 = ((XmlTextDescriptor) mixedChildText).getNextSibling();
        assertThat(mixedChildElement2).isNotNull();
        assertThat(mixedChildElement2).isInstanceOf(XmlElementDescriptor.class);
        assertThat(((XmlElementDescriptor) mixedChildElement2).getName()).isEqualTo("MixedChildElement2");
        assertThat(((XmlElementDescriptor) mixedChildElement2).getLineNumber()).isEqualTo(22);
        assertThat(childElement.getLastChild()).isEqualTo(mixedChildElement2);
    }

    @Test
    void invalidDocument() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/invalidDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(xmlFileDescriptor).isNotNull();
        assertThat(xmlFileDescriptor.isXmlWellFormed()).isEqualTo(false);
        store.commitTransaction();
    }

    @Test
    void schemaDocument() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/testSchema.xsd");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(xmlFileDescriptor).isNotNull();
        assertThat(xmlFileDescriptor.isXmlWellFormed()).isEqualTo(true);
        XmlElementDescriptor rootElement = xmlFileDescriptor.getRootElement();
        assertThat(rootElement).isNotNull();
        assertThat(rootElement.getName()).isEqualTo("schema");
        store.commitTransaction();
    }
}
