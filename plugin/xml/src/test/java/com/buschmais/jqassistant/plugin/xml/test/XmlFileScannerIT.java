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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
    void excludeXmlFile() {
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/validDocument.xml");
        FileDescriptor fileDescriptor = getScanner(Map.of("xml.file.exclude", "*.xml")).scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(fileDescriptor, not(instanceOf(XmlFileDescriptor.class)));
    }

    private void verifyDocument(XmlDocumentDescriptor xmlDocumentDescriptor) {
        assertThat(xmlDocumentDescriptor, notNullValue());
        assertThat(xmlDocumentDescriptor.isXmlWellFormed(), equalTo(true));
        assertThat(xmlDocumentDescriptor.getXmlVersion(), equalTo("1.0"));
        assertThat(xmlDocumentDescriptor.getCharacterEncodingScheme(), equalTo("UTF-8"));
        assertThat(xmlDocumentDescriptor.isStandalone(), equalTo(false));
        assertThat(xmlDocumentDescriptor.getLineNumber(), equalTo(1));
        XmlElementDescriptor rootElement = xmlDocumentDescriptor.getRootElement();
        assertThat(rootElement, notNullValue());
        assertThat(rootElement.getName(), equalTo("RootElement"));
        assertThat(rootElement.getLineNumber(), equalTo(2));
        List<XmlNamespaceDescriptor> rootNamespaces = rootElement.getDeclaredNamespaces();
        assertThat(rootNamespaces.size(), equalTo(1));
        XmlNamespaceDescriptor rootNS = rootNamespaces.get(0);
        assertThat(rootNS.getUri(), equalTo("http://jqassistant.org/plugin/xml/test/root"));
        assertThat(rootNS.getPrefix(), nullValue());
        List<XmlElementDescriptor> childElements = rootElement.getElements();
        assertThat(childElements.size(), equalTo(3));
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
        assertThat(childElement.getDeclaredNamespaces().size(), equalTo(0));
        assertThat(childElement.getLineNumber(), equalTo(3));
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size(), equalTo(1));
        XmlAttributeDescriptor childElementAttribute = childElementAttributes.get(0);
        assertThat(childElementAttribute.getName(), equalTo("attribute1"));
        List<XmlTextDescriptor> childElementTexts = childElement.getCharacters();
        assertThat(childElementTexts.size(), equalTo(1)); // non-coalescing StAX reports multiple text events, ensure it's activated
        XmlTextDescriptor childElementText = childElementTexts.get(0);
        assertThat(childElementText.getValue(), equalTo("Child\nText\nOn\nMultiple\nLines"));
        assertThat(childElementText.getLineNumber(), equalTo(9));
    }

    private void verifyExtraElement(XmlElementDescriptor childElement) {
        assertThat(childElement.getDeclaredNamespaces().size(), equalTo(1));
        assertThat(childElement.getLineNumber(), equalTo(10));
        XmlNamespaceDescriptor extraNamespace = childElement.getDeclaredNamespaces().get(0);
        assertThat(extraNamespace.getUri(), equalTo("http://jqassistant.org/plugin/xml/test/extra"));
        assertThat(extraNamespace.getPrefix(), equalTo("extra"));
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size(), equalTo(0));
        List<XmlElementDescriptor> extraChildElements = childElement.getElements();
        assertThat(extraChildElements.size(), equalTo(1));
        XmlElementDescriptor extraChildElement = extraChildElements.get(0);
        assertThat(extraChildElement.getName(), equalTo("ExtraChildElement"));
        assertThat(extraChildElement.getLineNumber(), equalTo(11));
        assertThat(extraChildElement.getNamespaceDeclaration(), equalTo(extraNamespace));
        List<XmlTextDescriptor> extraChildElementTexts = extraChildElement.getCharacters();
        assertThat(extraChildElementTexts.size(), equalTo(1));
        XmlTextDescriptor extraChildElementText = extraChildElementTexts.get(0);
        assertThat(extraChildElementText.getValue(), equalTo("Extra\nChild\nText\nOn\nMultiple\nLines"));
        assertThat(extraChildElementText.getLineNumber(), equalTo(17));
        List<XmlAttributeDescriptor> extraChildElementAttributes = extraChildElement.getAttributes();
        assertThat(extraChildElementAttributes.size(), equalTo(1));
        XmlAttributeDescriptor extraChildElementAttribute = extraChildElementAttributes.get(0);
        assertThat(extraChildElementAttribute.getName(), equalTo("attribute2"));
        assertThat(extraChildElementAttribute.getNamespaceDeclaration(), equalTo(extraNamespace));
    }

    private void verifyMixedParentElement(XmlElementDescriptor childElement) {
        assertThat(childElement.getLineNumber(), equalTo(19));
        XmlDescriptor mixedChildElement1 = childElement.getFirstChild();
        assertThat(mixedChildElement1, notNullValue());
        assertThat(mixedChildElement1, instanceOf(XmlElementDescriptor.class));
        assertThat(((XmlElementDescriptor) mixedChildElement1).getName(), equalTo("MixedChildElement1"));
        assertThat(((XmlElementDescriptor) mixedChildElement1).getLineNumber(), equalTo(20));
        XmlDescriptor mixedChildText = ((XmlElementDescriptor) mixedChildElement1).getNextSibling();
        assertThat(mixedChildText, notNullValue());
        assertThat(mixedChildText, instanceOf(XmlTextDescriptor.class));
        assertThat(((XmlTextDescriptor) mixedChildText).getValue(), equalTo("Mixed Parent Text"));
        assertThat(((XmlTextDescriptor) mixedChildText).getLineNumber(), equalTo(22));
        XmlDescriptor mixedChildElement2 = ((XmlTextDescriptor) mixedChildText).getNextSibling();
        assertThat(mixedChildElement2, notNullValue());
        assertThat(mixedChildElement2, instanceOf(XmlElementDescriptor.class));
        assertThat(((XmlElementDescriptor) mixedChildElement2).getName(), equalTo("MixedChildElement2"));
        assertThat(((XmlElementDescriptor) mixedChildElement2).getLineNumber(), equalTo(22));
        assertThat(childElement.getLastChild(), equalTo(mixedChildElement2));
    }

    @Test
    void invalidDocument() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/invalidDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(xmlFileDescriptor, notNullValue());
        assertThat(xmlFileDescriptor.isXmlWellFormed(), equalTo(false));
        store.commitTransaction();
    }

    @Test
    void schemaDocument() {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/testSchema.xsd");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(xmlFileDescriptor, notNullValue());
        assertThat(xmlFileDescriptor.isXmlWellFormed(), equalTo(true));
        XmlElementDescriptor rootElement = xmlFileDescriptor.getRootElement();
        assertThat(rootElement, notNullValue());
        assertThat(rootElement.getName(), equalTo("schema"));
        store.commitTransaction();
    }
}
