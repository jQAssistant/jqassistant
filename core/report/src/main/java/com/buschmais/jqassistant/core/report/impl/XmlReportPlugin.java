package com.buschmais.jqassistant.core.report.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.buschmais.jqassistant.core.report.api.*;
import com.buschmais.jqassistant.core.report.api.ReportPlugin.Default;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.source.ArtifactLocation;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.xo.api.CompositeObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link ReportPlugin} which writes the results of an
 * analysis to an XML file.
 */
@Slf4j
@Default
public class XmlReportPlugin implements ReportPlugin {

    // Properties
    public static final String XML_REPORT_FILE = "xml.report.file";

    // Default values
    public static final String DEFAULT_XML_REPORT_FILE = "jqassistant-report.xml";

    public static final String ENCODING = "UTF-8";

    public static final String NAMESPACE_URL = "http://schema.jqassistant.org/report/v2.0";

    private XMLOutputFactory xmlOutputFactory;

    private XMLStreamWriter xmlStreamWriter;

    private File xmlReportFile;

    private Result<? extends ExecutableRule> result;

    private long groupBeginTime;

    private long ruleBeginTime;

    private static final DateFormat XML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public void initialize() {
        this.xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        String xmlReport = (String) properties.get(XML_REPORT_FILE);
        this.xmlReportFile = xmlReport != null ? new File(xmlReport) : new File(reportContext.getOutputDirectory(), DEFAULT_XML_REPORT_FILE);
    }

    @Override
    public void begin() throws ReportException {
        run(() -> {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(xmlReportFile), ENCODING);
            XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(writer);
            xmlStreamWriter = new IndentingXMLStreamWriter(streamWriter);
            xmlStreamWriter.writeStartDocument(ENCODING, "1.0");
            xmlStreamWriter.setDefaultNamespace(NAMESPACE_URL);
            xmlStreamWriter.writeStartElement("jqassistant-report");
            xmlStreamWriter.writeDefaultNamespace(NAMESPACE_URL);
        });
    }

    @Override
    public void end() throws ReportException {
        run(() -> {
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndDocument();
            xmlStreamWriter.close();
        });
    }

    @Override
    public void beginConcept(Concept concept) {
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
    public void beginConstraint(Constraint constraint) {
        beginExecutable();
    }

    @Override
    public void endConstraint() throws ReportException {
        endRule();
    }

    @Override
    public void setResult(final Result<? extends ExecutableRule> result) {
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
                public void run() throws XMLStreamException {
                    xmlStreamWriter.writeStartElement(elementName);
                    xmlStreamWriter.writeAttribute("id", rule.getId());
                    writeElementWithCharacters("description", rule.getDescription());
                    if (!result.isEmpty()) {
                        xmlStreamWriter.writeStartElement("result");
                        xmlStreamWriter.writeStartElement("columns");
                        xmlStreamWriter.writeAttribute("count", Integer.toString(columnNames.size()));
                        xmlStreamWriter.writeAttribute("primary", primaryColumn);
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

    public File getXmlReportFile() {
        return xmlReportFile;
    }

    /**
     * Determine the primary column for a rule, i.e. the colum used by tools like
     * SonarQube to attach issues.
     *
     * @param rule The {@link ExecutableRule}.
     * @param columnNames The column names returned by the executed rule.
     * @return The name of the primary column.
     */
    private String getPrimaryColumn(ExecutableRule rule, List<String> columnNames) {
        if (columnNames == null || columnNames.isEmpty()) {
            return null;
        }
        String primaryColumn = rule.getReport().getPrimaryColumn();
        String firstColumn = columnNames.get(0);
        if (primaryColumn == null) {
            // primary column not explicitly specifed by the rule, so take the first column by default.
            return firstColumn;
        }
        if (!columnNames.contains(primaryColumn)) {
            log.warn("Rule '{}' defines primary column '{}' which is not provided by the result (available columns: {}). Falling back to '{}'.", rule,
                    primaryColumn, columnNames, firstColumn);
            primaryColumn = firstColumn;
        }
        return primaryColumn;
    }

    /**
     * Write the status of the current result.
     *
     * @throws XMLStreamException
     *             If a problem occurs.
     */
    private void writeStatus(Result.Status status) throws XMLStreamException {
        writeElementWithCharacters("status", status.name().toLowerCase());
    }

    /**
     * Determines the language and language element of a descriptor from a result
     * column.
     *
     * @param columnName
     *            The name of the column.
     * @param value
     *            The value.
     * @throws XMLStreamException
     *             If a problem occurs.
     */
    private void writeColumn(String columnName, Object value) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("column");
        xmlStreamWriter.writeAttribute("name", columnName);
        String label = null;
        if (value instanceof CompositeObject) {
            CompositeObject descriptor = (CompositeObject) value;
            LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
            if (elementValue != null) {
                xmlStreamWriter.writeStartElement("element");
                xmlStreamWriter.writeAttribute("language", elementValue.getLanguage());
                xmlStreamWriter.writeCharacters(elementValue.name());
                xmlStreamWriter.writeEndElement(); // element
                SourceProvider sourceProvider = elementValue.getSourceProvider();
                label = sourceProvider.getName(descriptor);
                Optional<FileLocation> sourceLocation = sourceProvider.getSourceLocation(descriptor);
                if (sourceLocation.isPresent()) {
                    xmlStreamWriter.writeStartElement("source");
                    writeSourceLocation(sourceLocation.get());
                    xmlStreamWriter.writeEndElement(); // sourceFile
                }
            }
        } else if (value != null) {
            label = ReportHelper.getLabel(value);
        }
        writeElementWithCharacters("value", label);
        xmlStreamWriter.writeEndElement(); // column
    }

    private void writeSourceLocation(FileLocation sourceLocation) throws XMLStreamException {
        xmlStreamWriter.writeAttribute("fileName", sourceLocation.getFileName());
        writeOptionalIntegerAttribute("startLine", sourceLocation.getStartLine());
        writeOptionalIntegerAttribute("endLine", sourceLocation.getEndLine());
        writeParentLocation(sourceLocation.getParent());
    }

    private void writeParentLocation(Optional<ArtifactLocation> optionalArtifactLocation) throws XMLStreamException {
        if (optionalArtifactLocation.isPresent()) {
            ArtifactLocation artifactLocation = optionalArtifactLocation.get();
            xmlStreamWriter.writeStartElement("parent");
            xmlStreamWriter.writeAttribute("fileName", artifactLocation.getFileName());
            writeOptionalStringAttribute("group", artifactLocation.getGroup());
            writeOptionalStringAttribute("name", artifactLocation.getName());
            writeOptionalStringAttribute("type", artifactLocation.getType());
            writeOptionalStringAttribute("version", artifactLocation.getVersion());
            writeOptionalStringAttribute("classifier", artifactLocation.getClassifier());
            writeParentLocation(artifactLocation.getParent());
            xmlStreamWriter.writeEndElement();
        }
    }

    private void writeElementWithCharacters(String element, String text) throws XMLStreamException {
        xmlStreamWriter.writeStartElement(element);
        xmlStreamWriter.writeCharacters(text);
        xmlStreamWriter.writeEndElement();
    }

    private void writeOptionalIntegerAttribute(String attribute, Optional<Integer> value) throws XMLStreamException {
        if (value.isPresent()) {
            xmlStreamWriter.writeAttribute(attribute, value.get().toString());
        }
    }

    private void writeOptionalStringAttribute(String attribute, Optional<String> value) throws XMLStreamException {
        if (value.isPresent()) {
            xmlStreamWriter.writeAttribute(attribute, value.get());
        }
    }

    /**
     * Writes the duration.
     *
     * @param beginTime
     *            The begin time.
     * @throws XMLStreamException
     *             If writing fails.
     */
    private void writeDuration(long beginTime) throws XMLStreamException {
        writeElementWithCharacters("duration", Long.toString(System.currentTimeMillis() - beginTime));
    }

    /**
     * Writes the severity of the rule.
     *
     * @param severity
     *            The severity the rule has been executed with
     * @throws XMLStreamException
     *             If writing fails.
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
     * @param operation
     *            The operation.
     * @throws ReportException
     *             If writing fails.
     */
    private void run(XmlOperation operation) throws ReportException {
        try {
            operation.run();
        } catch (XMLStreamException | IOException e) {
            throw new ReportException("Cannot write to XML report.", e);
        }
    }

    private interface XmlOperation {
        void run() throws XMLStreamException, IOException;
    }

}
