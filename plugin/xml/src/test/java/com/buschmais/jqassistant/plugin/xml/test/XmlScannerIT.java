package com.buschmais.jqassistant.plugin.xml.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 *
 */
public class XmlScannerIT extends AbstractPluginIT {

    /**
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void xmlDocument() throws IOException, AnalysisException {
        store.beginTransaction();
        File xmlFile = new File(getClassesDirectory(XmlScannerIT.class), "/test.xml");
//        File xmlFile = new File(getClassesDirectory(XmlScannerIT.class), "/ADELmetrology.xml");
        Descriptor descriptor = getScanner().scan(xmlFile, xmlFile.getAbsolutePath(), null);
        // MATCH (d:Xml:Document) RETURN d").getColumn("d")
        store.commitTransaction();
    }
}
