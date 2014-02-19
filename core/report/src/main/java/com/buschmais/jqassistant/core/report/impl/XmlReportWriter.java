package com.buschmais.jqassistant.core.report.impl;

import com.buschmais.jqassistant.core.analysis.api.ExecutionListener;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of an {@link com.buschmais.jqassistant.core.analysis.api.ExecutionListener} which writes the results of an analysis to an XML file.
 */
public class XmlReportWriter implements ExecutionListener {

    public static final String NAMESPACE_URL = "http://www.buschmais.com/jqassistant/core/report/schema/v1.0";
    public static final String NAMESPACE_PREFIX = "jqa-report";

    private static interface XmlOperation {
        void run() throws XMLStreamException;
    }

    private XMLStreamWriter xmlStreamWriter;

    private Result<? extends AbstractExecutable> result;

    private long groupBeginTime;

    private long executableBeginTime;

    private static final DateFormat XML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public XmlReportWriter(Writer writer) throws ExecutionListenerException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            xmlStreamWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(writer));
        } catch (XMLStreamException e) {
            throw new ExecutionListenerException("Cannot create XML stream writer.", e);
        }
    }

    @Override
    public void begin() throws ExecutionListenerException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeStartDocument();
                xmlStreamWriter.setPrefix(NAMESPACE_PREFIX, NAMESPACE_URL);
                xmlStreamWriter.writeStartElement(NAMESPACE_URL, "jqassistant-report");
                xmlStreamWriter.writeNamespace(NAMESPACE_PREFIX, NAMESPACE_URL);
            }
        });
    }

    @Override
    public void end() throws ExecutionListenerException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndDocument();
            }
        });
    }

    @Override
    public void beginConcept(Concept concept) throws ExecutionListenerException {
        beginExecutable();
    }

    @Override
    public void endConcept() throws ExecutionListenerException {
        endExecutable();
    }

    @Override
    public void beginGroup(final Group group) throws ExecutionListenerException {
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
    public void endGroup() throws ExecutionListenerException {
        run(new XmlOperation() {
            @Override
            public void run() throws XMLStreamException {
                writeDuration(groupBeginTime);
                xmlStreamWriter.writeEndElement();
            }
        });
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ExecutionListenerException {
        beginExecutable();
    }

    @Override
    public void endConstraint() throws ExecutionListenerException {
        endExecutable();
    }

    @Override
    public void setResult(final Result<? extends AbstractExecutable> result) throws ExecutionListenerException {
        this.result = result;
    }

    private void beginExecutable() {
        this.executableBeginTime = System.currentTimeMillis();
    }

    private void endExecutable() throws ExecutionListenerException {
        if (result != null) {
            final AbstractExecutable executable = result.getExecutable();
            final String elementName;
            if (executable instanceof Concept) {
                elementName = "concept";
            } else if (executable instanceof Constraint) {
                elementName = "constraint";
            } else {
                throw new ExecutionListenerException("Cannot write report for unsupported executable " + executable);
            }
            final List<String> columnNames = result.getColumnNames();
            run(new XmlOperation() {
                @Override
                public void run() throws XMLStreamException {
                    xmlStreamWriter.writeStartElement(elementName);
                    xmlStreamWriter.writeAttribute("id", executable.getId());
                    xmlStreamWriter.writeStartElement("description");
                    xmlStreamWriter.writeCharacters(executable.getDescription());
                    xmlStreamWriter.writeEndElement(); // description
                    if (!result.isEmpty()) {
                        xmlStreamWriter.writeStartElement("result");
                        xmlStreamWriter.writeStartElement("columns");
                        xmlStreamWriter.writeAttribute("count", Integer.toString(columnNames.size()));
                        for (String column : columnNames) {
                            xmlStreamWriter.writeStartElement("column");
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
                                String stringValue = value instanceof FullQualifiedNameDescriptor ? ((FullQualifiedNameDescriptor) value)
                                        .getFullQualifiedName() : value.toString();
                                xmlStreamWriter.writeStartElement("column");
                                xmlStreamWriter.writeAttribute("name", columnName);
                                xmlStreamWriter.writeCharacters(stringValue);
                                xmlStreamWriter.writeEndElement(); // column
                            }
                            xmlStreamWriter.writeEndElement();
                        }
                        xmlStreamWriter.writeEndElement(); // rows
                        xmlStreamWriter.writeEndElement(); // result
                    }
                    writeDuration(executableBeginTime);
                    xmlStreamWriter.writeEndElement(); // concept|constraint
                }
            });
        }
    }

    private void writeDuration(long beginTime) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("duration");
        xmlStreamWriter.writeCharacters(Long.toString(System.currentTimeMillis() - beginTime));
        xmlStreamWriter.writeEndElement(); // duration
    }

    private void run(XmlOperation operation) throws ExecutionListenerException {
        try {
            operation.run();
        } catch (XMLStreamException e) {
            throw new ExecutionListenerException("Cannot write to XML report.", e);
        }
    }
}
