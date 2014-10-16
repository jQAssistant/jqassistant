package com.buschmais.jqassistant.core.report.impl;

import java.io.InputStream;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;

public class HtmlReportTransformer implements ReportTransformer {

    @Override
    public void toEmbedded(Source source, Result target) throws ReportTransformerException {
        transform(source, target, HtmlReportTransformer.class.getResourceAsStream("/META-INF/xsl/jqassistant-report-embedded.xsl"));
    }

    @Override
    public void toStandalone(Source source, Result target) throws ReportTransformerException {
        transform(source, target, HtmlReportTransformer.class.getResourceAsStream("/META-INF/xsl/jqassistant-report-standalone.xsl"));
    }

    /**
     *
     /** Transforms the source to an HTML fragment which may be embedded into
     * other documents.
     *
     * @param source
     *            The source.
     * @param target
     *            The target.
     * @param template
     *            The input stream for the template.
     * @throws ReportTransformerException
     *             If transformation fails.
     */
    private void transform(Source source, Result target, InputStream template) throws ReportTransformerException {
        Source xsl = new StreamSource(template);
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
