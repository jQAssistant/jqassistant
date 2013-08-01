package com.buschmais.jqassistant.report.impl;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.buschmais.jqassistant.core.model.api.AbstractExecutable;
import com.buschmais.jqassistant.core.model.api.Concept;
import com.buschmais.jqassistant.core.model.api.Constraint;
import com.buschmais.jqassistant.core.model.api.ConstraintGroup;
import com.buschmais.jqassistant.core.model.api.Result;
import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Implementation of a {@link ReportWriter} which writes the results of an analysis to an XML file.
 */
public class XmlReportWriter implements ReportWriter {

    public static final String NAMESPACE_URL = "http://www.buschmais.com/jqassistant/core/report/schema/v1.0";
    public static final String NAMESPACE_PREFIX = "jqa-report";

    private static interface XmlOperation {
        void run() throws XMLStreamException;
    }

    private XMLStreamWriter xmlStreamWriter;

    private AbstractExecutable executable;

    private Result<? extends AbstractExecutable> result;

    private long constraintGroupBeginTime;

    private long executableBeginTime;

	private static final DateFormat XML_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

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
    public void beginConcept(Concept concept) throws ReportWriterException {
        beginExecutable(concept);
    }

    @Override
    public void endConcept() throws ReportWriterException {
        endExecutable();
    }

    @Override
    public void beginConstraintGroup(final ConstraintGroup constraintGroup) throws ReportWriterException {
		final Date now = new Date();
		run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeStartElement("constraintGroup");
                xmlStreamWriter.writeAttribute("id", constraintGroup.getId());
				xmlStreamWriter.writeAttribute("date",
						XML_DATE_FORMAT.format(now));
            }
        });
		this.constraintGroupBeginTime = now.getTime();
    }

    @Override
    public void endConstraintGroup() throws ReportWriterException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                writeDuration(constraintGroupBeginTime);
                xmlStreamWriter.writeEndElement();
            }
        });
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportWriterException {
        beginExecutable(constraint);
    }

    @Override
    public void endConstraint() throws ReportWriterException {
        endExecutable();
    }

    @Override
    public void setResult(final Result result) throws ReportWriterException {
        this.result = result;
    }

    private void beginExecutable(AbstractExecutable executable) {
        this.executable = executable;
        this.executableBeginTime = System.currentTimeMillis();
    }

    private void endExecutable() throws ReportWriterException {
        final long duration = (System.currentTimeMillis() - this.executableBeginTime);
        final AbstractExecutable executable = result.getExecutable();
        final String elementName;
        if (executable instanceof Concept) {
            elementName = "concept";
        } else if (executable instanceof Constraint) {
            elementName = "constraint";
        } else {
            throw new ReportWriterException("Cannot write report for unsupported executable " + executable);
        }
        final List<String> columnNames = result.getColumnNames();
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeStartElement(elementName);
                xmlStreamWriter.writeAttribute("id", executable.getId());
                xmlStreamWriter.writeStartElement("description");
                xmlStreamWriter.writeCharacters(executable.getDescription());
                xmlStreamWriter.writeEndElement(); //description
                if (!result.isEmpty()) {
                    xmlStreamWriter.writeStartElement("result");
                    xmlStreamWriter.writeStartElement("columns");
                    xmlStreamWriter.writeAttribute("count", Integer.toString(columnNames.size()));
                    for (String column : columnNames) {
                        xmlStreamWriter.writeStartElement("column");
                        xmlStreamWriter.writeCharacters(column);
                        xmlStreamWriter.writeEndElement(); //column
                    }
                    xmlStreamWriter.writeEndElement(); //columns
                    xmlStreamWriter.writeStartElement("rows");
                    List<Map<String, Object>> rows = result.getRows();
                    xmlStreamWriter.writeAttribute("count", Integer.toString(rows.size()));
                    for (Map<String, Object> row : rows) {
                        xmlStreamWriter.writeStartElement("row");
                        for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
                            String columnName = rowEntry.getKey();
                            Object value = rowEntry.getValue();
                            String stringValue = value instanceof AbstractDescriptor ? ((AbstractDescriptor) value).getFullQualifiedName() : value.toString();
                            xmlStreamWriter.writeStartElement("column");
                            xmlStreamWriter.writeAttribute("name", columnName);
                            xmlStreamWriter.writeCharacters(stringValue);
                            xmlStreamWriter.writeEndElement(); //column
                        }
                        xmlStreamWriter.writeEndElement();
                    }
                    xmlStreamWriter.writeEndElement(); // rows
                    xmlStreamWriter.writeEndElement(); //result
                }
                writeDuration(executableBeginTime);
                xmlStreamWriter.writeEndElement(); // concept|constraint
            }
        });
    }

    private void writeDuration(long beginTime) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("duration");
        xmlStreamWriter.writeCharacters(Long.toString(System.currentTimeMillis() - beginTime));
        xmlStreamWriter.writeEndElement(); // duration
    }

    private void run(XmlOperation operation) throws ReportWriterException {
        try {
            operation.run();
        } catch (XMLStreamException e) {
            throw new ReportWriterException("Cannot write to XML report.", e);
        }
    }
}
