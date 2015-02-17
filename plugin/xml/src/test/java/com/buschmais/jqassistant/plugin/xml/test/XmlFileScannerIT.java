package com.buschmais.jqassistant.plugin.xml.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.xml.api.model.*;

/**
 * Tests the generic XML scanner.
 */
public class XmlFileScannerIT extends AbstractPluginIT {

    /**
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void validDocument() throws IOException, AnalysisException {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/validDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), null);
        assertThat(xmlFileDescriptor, notNullValue());
        assertThat(xmlFileDescriptor.isWellFormed(), equalTo(true));
        assertThat(xmlFileDescriptor.getVersion(), equalTo("1.0"));
        assertThat(xmlFileDescriptor.getCharacterEncodingScheme(), equalTo("UTF-8"));
        assertThat(xmlFileDescriptor.isStandalone(), equalTo(false));
        XmlElementDescriptor rootElement = xmlFileDescriptor.getRootElement();
        assertThat(rootElement, notNullValue());
        assertThat(rootElement.getName(), equalTo("RootElement"));
        List<XmlNamespaceDescriptor> rootNamespaces = rootElement.getDeclaredNamespaces();
        assertThat(rootNamespaces.size(), equalTo(1));
        XmlNamespaceDescriptor rootNS = rootNamespaces.get(0);
        assertThat(rootNS.getUri(), equalTo("http://jqassistant.org/plugin/xml/test/root"));
        assertThat(rootNS.getPrefix(), nullValue());
        List<XmlElementDescriptor> childElements = rootElement.getElements();
        assertThat(childElements.size(), equalTo(2));
        for (XmlElementDescriptor childElement : childElements) {
            if ("ChildElement".equals(childElement.getName())) {
                assertThat(childElement.getDeclaredNamespaces().size(), equalTo(0));
                List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
                assertThat(childElementAttributes.size(), equalTo(1));
                XmlAttributeDescriptor childElementAttribute = childElementAttributes.get(0);
                assertThat(childElementAttribute.getName(), equalTo("attribute1"));
                List<XmlTextDescriptor> childElementTexts = childElement.getCharacters();
                assertThat(childElementTexts.size(), equalTo(1));
                XmlTextDescriptor childElementText = childElementTexts.get(0);
                assertThat(childElementText.getValue(), equalTo("Child Text"));
            } else if ("ExtraElement".equals(childElement.getName())) {
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
            } else {
                fail("Found unexpected child element: " + childElement.getName());
            }
        }
        store.commitTransaction();
    }

    @Test
    public void invalidDocument() throws IOException, AnalysisException {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/invalidDocument.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), null);
        assertThat(xmlFileDescriptor, notNullValue());
        assertThat(xmlFileDescriptor.isWellFormed(), equalTo(false));
        store.commitTransaction();
    }
}
