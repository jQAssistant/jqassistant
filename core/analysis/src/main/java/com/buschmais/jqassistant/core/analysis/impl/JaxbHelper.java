package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 29.07.13
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public class JaxbHelper {

    public static Schema getSchema(String resource) {
        Schema schema;
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(RuleSetReader.class.getResource(resource));
        } catch (SAXException e) {
            throw new IllegalStateException("Cannot read rules schema.", e);
        }
        return schema;
    }
}
