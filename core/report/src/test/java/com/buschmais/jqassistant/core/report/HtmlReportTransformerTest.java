package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Verifies the functionality of the HTML report transformer.
 */
public class HtmlReportTransformerTest {

    @Test
    public void transform() throws ReportException, ReportTransformerException, FileNotFoundException {
        File xmlReport = new XmlReportTestHelper().createXmlReport();
        HtmlReportTransformer transformer = new HtmlReportTransformer();
        Source xmlSource = new StreamSource(new FileReader(xmlReport));
        StringWriter htmlWriter = new StringWriter();
        javax.xml.transform.Result htmlTarget = new StreamResult(htmlWriter);
        transformer.toEmbedded(xmlSource, htmlTarget);
        String html = htmlWriter.toString();
        assertThat(html, containsString("my:concept"));
    }
}
