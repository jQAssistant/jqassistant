package com.buschmais.jqassistant.report.impl;

import com.buschmais.jqassistant.core.model.api.*;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 28.07.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class XmlReportWriter implements ReportWriter {

    public static final String NAMESPACE_URL = "http://www.buschmais.com/jqassistant/core/report/schema/v1.0";
    public static final String NAMESPACE_PREFIX = "jqa-report";

    private static interface XmlOperation {
        void run() throws XMLStreamException;
    }

    private XMLStreamWriter xmlStreamWriter;

    public XmlReportWriter(Writer writer) throws ReportWriterException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            xmlStreamWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(writer));
        } catch (XMLStreamException e) {
            throw new ReportWriterException("Cannot create XML stream writer.", e);
        }
    }

    @Override
    public void begin() throws ReportWriterException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeStartDocument();
                xmlStreamWriter.setPrefix(NAMESPACE_PREFIX, NAMESPACE_URL);
                xmlStreamWriter.setDefaultNamespace(NAMESPACE_URL);
                xmlStreamWriter.writeStartElement(NAMESPACE_URL, "jqassistant-report");
                xmlStreamWriter.writeNamespace(NAMESPACE_PREFIX, NAMESPACE_URL);
                xmlStreamWriter.writeDefaultNamespace(NAMESPACE_URL);
            }
        });
    }

    @Override
    public void end() throws ReportWriterException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndDocument();
            }
        });
    }

    @Override
    public void beginConcept(final Concept concept) throws ReportWriterException {
    }

    @Override
    public void endConcept() throws ReportWriterException {
    }

    @Override
    public void beginConstraintGroup(ConstraintGroup constraintGroup) throws ReportWriterException {
    }

    @Override
    public void endConstraintGroup() throws ReportWriterException {
    }

    @Override
    public void beginConstraint(final Constraint constraint) throws ReportWriterException {
    }

    @Override
    public void endConstraint() throws ReportWriterException {
    }

    @Override
    public void setResult(Result result) throws ReportWriterException {
        final AbstractExecutable executable = result.getExecutable();
        final String elementName;
        if (executable instanceof Concept) {
            elementName = "concept";
        } else if (executable instanceof Constraint) {
            elementName = "constraint";
        } else {
            throw new ReportWriterException("Cannot write report for unsupported executable " + executable);
        }
        final List<Map<String, Object>> rows = result.getRows();
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                if (rows.isEmpty()) {
                    xmlStreamWriter.writeEmptyElement(elementName);
                    xmlStreamWriter.writeAttribute("id", executable.getId());
                } else {
                    xmlStreamWriter.writeStartElement(elementName);
                    xmlStreamWriter.writeAttribute("id", executable.getId());
                    for (Map<String, Object> row : rows) {
                        xmlStreamWriter.writeStartElement("row");
                        for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
                            String columnName = rowEntry.getKey();
                            Object value = rowEntry.getValue();
                            xmlStreamWriter.writeStartElement("column");
                            xmlStreamWriter.writeAttribute("name", columnName);
                            xmlStreamWriter.writeCharacters(value.toString());
                            xmlStreamWriter.writeEndElement();
                        }
                        xmlStreamWriter.writeEndElement();
                    }
                    xmlStreamWriter.writeEndElement();
                }
            }
        });
    }

    private void run(XmlOperation operation) throws ReportWriterException {
        try {
            operation.run();
        } catch (XMLStreamException e) {
            throw new ReportWriterException("Cannot write to XML report.", e);
        }
    }
}
