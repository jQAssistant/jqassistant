package com.buschmais.jqassistant.core.report.impl;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.*;
import com.buschmais.xo.api.CompositeObject;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Implementation of {@link ReportPlugin} which writes the results of an
 * analysis to an XML file.
 */
public class XmlReportWriter implements ReportPlugin {

    public static final String TYPE = "xml";

    public static final String ENCODING = "UTF-8";

    public static final String NAMESPACE_URL = "http://www.buschmais.com/jqassistant/core/report/schema/v1.3";
    public static final String NAMESPACE_PREFIX = "jqa-report";

    private interface XmlOperation {
        void run() throws XMLStreamException, ReportException;
    }

    private XMLStreamWriter xmlStreamWriter;

    private Result<? extends ExecutableRule> result;

    private long groupBeginTime;

    private long ruleBeginTime;

    private static final DateFormat XML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public XmlReportWriter(Writer writer) throws ReportException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            xmlStreamWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(writer));
        } catch (XMLStreamException e) {
            throw new ReportException("Cannot create XML stream writer.", e);
        }
    }

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
    }

    @Override
    public void begin() throws ReportException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeStartDocument(ENCODING, "1.0");
                xmlStreamWriter.setPrefix(NAMESPACE_PREFIX, NAMESPACE_URL);
                xmlStreamWriter.writeStartElement(NAMESPACE_URL, "jqassistant-report");
                xmlStreamWriter.writeNamespace(NAMESPACE_PREFIX, NAMESPACE_URL);
            }
        });
    }

    @Override
    public void end() throws ReportException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndDocument();
            }
        });
        try {
            xmlStreamWriter.close();
        } catch (XMLStreamException e) {
            throw new ReportException("Cannot close XML stream writer", e);
        }
    }

    @Override
    public void beginConcept(Concept concept) throws ReportException {
        beginExecutable();
    }

    @Override
    public void endConcept() throws ReportException {
        endRule();
    }

    @Override
    public void beginGroup(final Group group) throws ReportException {
        final Date now = new Date();
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeStartElement("group");
                xmlStreamWriter.writeAttribute("id", group.getId());
                xmlStreamWriter.writeAttribute("date", XML_DATE_FORMAT.format(now));
            }
        });
        this.groupBeginTime = now.getTime();
    }

    @Override
    public void endGroup() throws ReportException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                writeDuration(groupBeginTime);
                xmlStreamWriter.writeEndElement();
            }
        });
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportException {
        beginExecutable();
    }

    @Override
    public void endConstraint() throws ReportException {
        endRule();
    }

    @Override
    public void setResult(final Result<? extends ExecutableRule> result) throws ReportException {
        this.result = result;
    }

    private void beginExecutable() {
        this.ruleBeginTime = System.currentTimeMillis();
    }

    private void endRule() throws ReportException {
        if (result != null) {
            final ExecutableRule rule = result.getRule();
            final String elementName;
            if (rule instanceof Concept) {
                elementName = "concept";
            } else if (rule instanceof Constraint) {
                elementName = "constraint";
            } else {
                throw new ReportException("Cannot write report for unsupported rule " + rule);
            }
            final List<String> columnNames = result.getColumnNames();
            final String primaryColumn = getPrimaryColumn(rule, columnNames);
            run(new XmlOperation() {
                @Override
                public void run() throws XMLStreamException, ReportException {
                    xmlStreamWriter.writeStartElement(elementName);
                    xmlStreamWriter.writeAttribute("id", rule.getId());
                    xmlStreamWriter.writeStartElement("description");
                    xmlStreamWriter.writeCharacters(rule.getDescription());
                    xmlStreamWriter.writeEndElement(); // description
                    if (!result.isEmpty()) {
                        xmlStreamWriter.writeStartElement("result");
                        xmlStreamWriter.writeStartElement("columns");
                        xmlStreamWriter.writeAttribute("count", Integer.toString(columnNames.size()));
                        for (String column : columnNames) {
                            xmlStreamWriter.writeStartElement("column");
                            if (primaryColumn.equals(column)) {
                                xmlStreamWriter.writeAttribute("primary", Boolean.TRUE.toString());
                            }
                            xmlStreamWriter.writeCharacters(column);
                            xmlStreamWriter.writeEndElement(); // column
                        }
                        xmlStreamWriter.writeEndElement(); // columns
                        xmlStreamWriter.writeStartElement("rows");
                        List<Map<String, Object>> rows = result.getRows();
                        xmlStreamWriter.writeAttribute("count", Integer.toString(rows.size()));
                        for (Map<String, Object> row : rows) {
                            xmlStreamWriter.writeStartElement("row");
                            for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
                                String columnName = rowEntry.getKey();
                                Object value = rowEntry.getValue();
                                writeColumn(columnName, value);
                            }
                            xmlStreamWriter.writeEndElement();
                        }
                        xmlStreamWriter.writeEndElement(); // rows
                        xmlStreamWriter.writeEndElement(); // result
                    }
                    writeStatus(result.getStatus()); // status
                    writeSeverity(result.getSeverity()); // severity
                    writeDuration(ruleBeginTime);
                    xmlStreamWriter.writeEndElement(); // concept|constraint
                }
            });
        }
    }

    private String getPrimaryColumn(ExecutableRule rule, List<String> columnNames) {
        String primaryColumn = rule.getReport().getPrimaryColumn();
        if (primaryColumn == null && columnNames != null && !columnNames.isEmpty()) {
            primaryColumn = columnNames.get(0);
        }
        return primaryColumn;
    }

    /**
     * Write the status of the current result.
     *
     * @throws XMLStreamException If a problem occurs.
     */
    private void writeStatus(Result.Status status) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("status");
        xmlStreamWriter.writeCharacters(status.name().toLowerCase());
        xmlStreamWriter.writeEndElement();
    }

    /**
     * Determines the language and language element of a descriptor from a
     * result column.
     *
     * @param columnName The name of the column.
     * @param value      The value.
     * @throws XMLStreamException                                                    If a problem occurs.
     * @throws ReportException If a problem occurs.
     */
    private void writeColumn(String columnName, Object value) throws XMLStreamException, ReportException {
        xmlStreamWriter.writeStartElement("column");
        xmlStreamWriter.writeAttribute("name", columnName);
        String stringValue = null;
        if (value instanceof CompositeObject) {
            CompositeObject descriptor = (CompositeObject) value;
            LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
            if (elementValue != null) {
                xmlStreamWriter.writeStartElement("element");
                xmlStreamWriter.writeAttribute("language", elementValue.getLanguage());
                xmlStreamWriter.writeCharacters(elementValue.name());
                xmlStreamWriter.writeEndElement(); // element
                SourceProvider sourceProvider = elementValue.getSourceProvider();
                stringValue = sourceProvider.getName(descriptor);
                String sourceFile = sourceProvider.getSourceFile(descriptor);
                Integer lineNumber = sourceProvider.getLineNumber(descriptor);
                if (sourceFile != null) {
                    xmlStreamWriter.writeStartElement("source");
                    xmlStreamWriter.writeAttribute("name", sourceFile);
                    if (lineNumber != null) {
                        xmlStreamWriter.writeAttribute("line", lineNumber.toString());
                    }
                    xmlStreamWriter.writeEndElement(); // sourceFile
                }
            }
        } else if (value != null) {
            stringValue = ReportHelper.getLabel(value);
        }
        xmlStreamWriter.writeStartElement("value");
        xmlStreamWriter.writeCharacters(stringValue);
        xmlStreamWriter.writeEndElement(); // value
        xmlStreamWriter.writeEndElement(); // column
    }

    /**
     * Writes the duration.
     *
     * @param beginTime The begin time.
     * @throws XMLStreamException If writing fails.
     */
    private void writeDuration(long beginTime) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("duration");
        xmlStreamWriter.writeCharacters(Long.toString(System.currentTimeMillis() - beginTime));
        xmlStreamWriter.writeEndElement(); // duration
    }

    /**
     * Writes the severity of the rule.
     *
     * @param severity The severity the rule has been executed with
     * @throws XMLStreamException If writing fails.
     */
    private void writeSeverity(Severity severity) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("severity");
        xmlStreamWriter.writeAttribute("level", severity.getLevel().toString());
        xmlStreamWriter.writeCharacters(severity.getValue());
        xmlStreamWriter.writeEndElement();
    }

    /**
     * Defines an operation to write XML elements.
     *
     * @param operation The operation.
     * @throws ReportException If writing fails.
     */
    private void run(XmlOperation operation) throws ReportException {
        try {
            operation.run();
        } catch (XMLStreamException e) {
            throw new ReportException("Cannot write to XML report.", e);
        }
    }
}
