package com.buschmais.jqassistant.core.report.impl;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;

public class HtmlReportTransformer implements ReportTransformer {

    @Override
    public void transform(Source source, Result target) throws ReportTransformerException {
        Source xsl = new StreamSource(HtmlReportTransformer.class.getResourceAsStream("/META-INF/xsl/jqassistant-report-embedded.xsl"));
        TransformerFactory transformerFactory;
        Transformer transformer;
        try {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) throws TransformerException {
                    return new StreamSource(HtmlReportTransformer.class.getResourceAsStream(href));
                }
            });
            transformer = transformerFactory.newTransformer(xsl);
        } catch (TransformerConfigurationException e) {
            throw new ReportTransformerException("Cannot get transformer factory.", e);
        }
        try {
            transformer.transform(source, target);
        } catch (TransformerException e) {
            throw new ReportTransformerException("Cannot transform report.", e);
        }
    }
}
