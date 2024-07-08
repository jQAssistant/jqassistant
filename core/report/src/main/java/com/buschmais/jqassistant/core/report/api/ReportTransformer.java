package com.buschmais.jqassistant.core.report.api;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Defines functionality to transform an XML report to HTML representation.
 */
public interface ReportTransformer {

    /**
     * Transforms the source to an HTML fragment which may be embedded into
     * other documents.
     * 
     * @param source
     *            The source.
     * @param target
     *            The target.
     * @throws ReportTransformerException
     *             If transformation fails.
     */
    void toEmbedded(Source source, Result target) throws ReportTransformerException;

    /**
     * Transforms the source to a standalone HTML document.
     *
     * @param source
     *            The source.
     * @param target
     *            The target.
     * @throws ReportTransformerException
     *             If transformation fails.
     */
    void toStandalone(Source source, Result target) throws ReportTransformerException;
}
