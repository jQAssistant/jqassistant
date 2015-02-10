package com.buschmais.jqassistant.plugin.xml.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.xml.api.model.*;

/**
 *
 */
public class XmlFileScannerIT extends AbstractPluginIT {

    /**
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void xmlDocument() throws IOException, AnalysisException {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlFileScannerIT.class), "/test.xml");
        XmlFileDescriptor xmlFileDescriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), null);
        assertThat(xmlFileDescriptor, notNullValue());
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
        XmlElementDescriptor childElement = childElements.get(0);
        assertThat(childElement.getName(), equalTo("ChildElement"));
        assertThat(childElement.getDeclaredNamespaces().size(), equalTo(0));
        List<XmlAttributeDescriptor> childElementAttributes = childElement.getAttributes();
        assertThat(childElementAttributes.size(), equalTo(1));
        XmlAttributeDescriptor childElementAttribute = childElementAttributes.get(0);
        assertThat(childElementAttribute.getName(), equalTo("attribute1"));
        List<XmlTextDescriptor> childElementText = childElement.getCharacters();
        assertThat(childElementText.size(), equalTo(1));
        store.commitTransaction();
    }
}
