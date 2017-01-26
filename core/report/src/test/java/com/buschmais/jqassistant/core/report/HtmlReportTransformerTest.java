package com.buschmais.jqassistant.core.report;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;

/**
 * Verifies the functionality of the HTML report transformer.
 */
public class HtmlReportTransformerTest {

    @Test
    public void transform() throws ReportException, SAXException, JAXBException, ReportTransformerException {
        String xmlReport = XmlReportTestHelper.createXmlReport();
        HtmlReportTransformer transformer = new HtmlReportTransformer();
        Source xmlSource = new StreamSource(new StringReader(xmlReport));
        StringWriter htmlWriter = new StringWriter();
        javax.xml.transform.Result htmlTarget = new StreamResult(htmlWriter);
        transformer.toEmbedded(xmlSource, htmlTarget);
        String html = htmlWriter.toString();
        assertThat(html, containsString("my:concept"));
    }
}
