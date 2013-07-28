package com.buschmais.jqassistant.report.api;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 28.07.13
 * Time: 18:26
 * To change this template use File | Settings | File Templates.
 */
public interface ReportTransformer {

    void transform(Source source, Result target) throws ReportTransformerException;
}
