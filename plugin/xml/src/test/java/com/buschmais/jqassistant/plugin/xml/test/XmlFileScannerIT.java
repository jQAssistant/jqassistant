package com.buschmais.jqassistant.plugin.xml.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.xml.api.model.*;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

/**
 * Tests the generic XML scanner.
 */
public class XmlFileScannerIT extends AbstractPluginIT {

    /**
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void validXmlSource() throws IOException, AnalysisException {
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
    public void validXmlFile() throws IOException, AnalysisException {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/validDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), XmlScope.DOCUMENT);
        verifyDocument(xmlFileDescriptor);
        store.commitTransaction();
    }

    private void verifyDocument(XmlDocumentDescriptor xmlDocumentDescriptor) {
        assertThat(xmlDocumentDescriptor, notNullValue());
        assertThat(xmlDocumentDescriptor.isXmlWellFormed(), equalTo(true));
        assertThat(xmlDocumentDescriptor.getXmlVersion(), equalTo("1.0"));
        assertThat(xmlDocumentDescriptor.getCharacterEncodingScheme(), equalTo("UTF-8"));
        assertThat(xmlDocumentDescriptor.isStandalone(), equalTo(false));
        XmlElementDescriptor rootElement = xmlDocumentDescriptor.getRootElement();
        assertThat(rootElement, notNullValue());
        assertThat(rootElement.getName(), equalTo("RootElement"));
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
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size(), equalTo(1));
        XmlAttributeDescriptor childElementAttribute = childElementAttributes.get(0);
        assertThat(childElementAttribute.getName(), equalTo("attribute1"));
        List<XmlTextDescriptor> childElementTexts = childElement.getCharacters();
        assertThat(childElementTexts.size(), equalTo(1));
        XmlTextDescriptor childElementText = childElementTexts.get(0);
        assertThat(childElementText.getValue(), equalTo("Child Text"));
    }

    private void verifyExtraElement(XmlElementDescriptor childElement) {
        assertThat(childElement.getDeclaredNamespaces().size(), equalTo(1));
        XmlNamespaceDescriptor extraNamespace = childElement.getDeclaredNamespaces().get(0);
        assertThat(extraNamespace.getUri(), equalTo("http://jqassistant.org/plugin/xml/test/extra"));
        assertThat(extraNamespace.getPrefix(), equalTo("extra"));
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size(), equalTo(0));
        List<XmlElementDescriptor> extraChildElements = childElement.getElements();
        assertThat(extraChildElements.size(), equalTo(1));
        XmlElementDescriptor extraChildElement = extraChildElements.get(0);
        assertThat(extraChildElement.getName(), equalTo("ExtraChildElement"));
        assertThat(extraChildElement.getNamespaceDeclaration(), equalTo(extraNamespace));
        List<XmlTextDescriptor> extraChildElementTexts = extraChildElement.getCharacters();
        assertThat(extraChildElementTexts.size(), equalTo(1));
        XmlTextDescriptor extraChildElementText = extraChildElementTexts.get(0);
        assertThat(extraChildElementText.getValue(), equalTo("Extra Child Text"));
        List<XmlAttributeDescriptor> extraChildElementAttributes = extraChildElement.getAttributes();
        assertThat(extraChildElementAttributes.size(), equalTo(1));
        XmlAttributeDescriptor extraChildElementAttribute = extraChildElementAttributes.get(0);
        assertThat(extraChildElementAttribute.getName(), equalTo("attribute2"));
        assertThat(extraChildElementAttribute.getNamespaceDeclaration(), equalTo(extraNamespace));
    }

    private void verifyMixedParentElement(XmlElementDescriptor childElement) {
        XmlDescriptor mixedChildElement1 = childElement.getFirstChild();
        assertThat(mixedChildElement1, notNullValue());
        assertThat(mixedChildElement1, instanceOf(XmlElementDescriptor.class));
        assertThat(((XmlElementDescriptor) mixedChildElement1).getName(), equalTo("MixedChildElement1"));
        XmlDescriptor mixedChildText = ((XmlElementDescriptor) mixedChildElement1).getNextSibling();
        assertThat(mixedChildText, notNullValue());
        assertThat(mixedChildText, instanceOf(XmlTextDescriptor.class));
        assertThat(((XmlTextDescriptor) mixedChildText).getValue(), equalTo("Mixed Parent Text"));
        XmlDescriptor mixedChildElement2 = ((XmlTextDescriptor) mixedChildText).getNextSibling();
        assertThat(mixedChildElement2, notNullValue());
        assertThat(mixedChildElement2, instanceOf(XmlElementDescriptor.class));
        assertThat(((XmlElementDescriptor) mixedChildElement2).getName(), equalTo("MixedChildElement2"));
        assertThat(childElement.getLastChild(), equalTo(mixedChildElement2));
    }

    @Test
    public void invalidDocument() throws IOException, AnalysisException {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/invalidDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), XmlScope.DOCUMENT);
        assertThat(xmlFileDescriptor, notNullValue());
        assertThat(xmlFileDescriptor.isXmlWellFormed(), equalTo(false));
        store.commitTransaction();
    }

    @Test
    public void schemaDocument() throws IOException, AnalysisException {
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
