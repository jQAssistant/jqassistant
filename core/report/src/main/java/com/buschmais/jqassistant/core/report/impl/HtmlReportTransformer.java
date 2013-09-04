package com.buschmais.jqassistant.core.report.impl;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 28.07.13
 * Time: 18:27
 * To change this template use File | Settings | File Templates.
 */
public class HtmlReportTransformer implements ReportTransformer {

    @Override
    public void transform(Source source, Result target) throws ReportTransformerException {
        Source xsl = new StreamSource(HtmlReportTransformer.class.getResourceAsStream("/xsl/jqassistant-report-embedded.xsl"));
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
