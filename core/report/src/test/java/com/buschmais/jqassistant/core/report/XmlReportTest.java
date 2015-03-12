package com.buschmais.jqassistant.core.report;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.report.schema.v1.*;

public class XmlReportTest {

    @Test
    public void writeAndReadReport() throws JAXBException, SAXException, AnalysisListenerException {
        String xmlReport = XmlReportTestHelper.createXmlReport();

        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsdFactory.newSchema(new StreamSource(XmlReportTest.class.getResourceAsStream("/META-INF/xsd/jqassistant-report-1.0.xsd")));
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        StreamSource streamSource = new StreamSource(new StringReader(xmlReport));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        JqassistantReport report = unmarshaller.unmarshal(streamSource, JqassistantReport.class).getValue();
        assertThat(report, notNullValue());
        assertThat(report.getGroup().size(), equalTo(1));
        GroupType groupType = report.getGroup().get(0);
        assertThat(groupType.getDate(), notNullValue());
        assertThat(groupType.getId(), equalTo("default"));
        assertThat(groupType.getConceptOrConstraint().size(), equalTo(1));
        RuleType ruleType = groupType.getConceptOrConstraint().get(0);
        assertThat(ruleType.getStatus(), equalTo(StatusEnumType.SUCCESS));
        assertThat(ruleType, instanceOf(ConceptType.class));
        assertThat(ruleType.getId(), equalTo("my:concept"));
        assertThat(ruleType.getDescription(), equalTo("My concept description"));
        assertThat(ruleType.getResult(), notNullValue());
        ResultType result = ruleType.getResult();
        assertThat(result.getColumns().getCount(), equalTo(2));
        List<ColumnHeaderType> columnHeaders = result.getColumns().getColumn();
        assertThat(columnHeaders.size(), equalTo(2));
        verifyColumnHeader(columnHeaders.get(0), "c1", false);
        verifyColumnHeader(columnHeaders.get(1), "c2", true);
        assertThat(result.getRows().getCount(), equalTo(1));
        List<RowType> rows = result.getRows().getRow();
        assertThat(rows.size(), equalTo(1));
        RowType rowType = rows.get(0);
        assertThat(rowType.getColumn().size(), equalTo(2));
        for (ColumnType column : rowType.getColumn()) {
            assertThat(column.getName(), anyOf(equalTo("c1"), equalTo("c2")));
            if ("c1".equals(column.getName())) {
                assertThat(column.getValue(), equalTo("simpleValue"));
            } else if ("c2".equals(column.getName())) {
                assertThat(column.getElement().getLanguage(), equalTo("TestLanguage"));
                assertThat(column.getElement().getValue(), equalTo("TestElement"));
                assertThat(column.getValue(), equalTo("descriptorValue"));
                SourceType source = column.getSource();
                assertThat(source.getName(), equalTo("Test.java"));
                assertThat(source.getLine(), equalTo(1));
            }
        }
    }

    private void verifyColumnHeader(ColumnHeaderType columnHeaderC1, String expectedName, boolean isPrimary) {
        assertThat(columnHeaderC1.getValue(), equalTo(expectedName));
        assertThat(columnHeaderC1.isPrimary(), equalTo(isPrimary));
    }

    @Test
    public void testReportWithConstraint() throws JAXBException, SAXException, AnalysisListenerException {
        String xmlReport = XmlReportTestHelper.createXmlReportWithConstraints();
        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsdFactory.newSchema(new StreamSource(XmlReportTest.class.getResourceAsStream("/META-INF/xsd/jqassistant-report-1.0.xsd")));
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        StreamSource streamSource = new StreamSource(new StringReader(xmlReport));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        JqassistantReport report = unmarshaller.unmarshal(streamSource, JqassistantReport.class).getValue();
        assertThat(report, notNullValue());
        assertThat(report.getGroup().size(), equalTo(1));
        GroupType groupType = report.getGroup().get(0);
        assertThat(groupType.getDate(), notNullValue());
        assertThat(groupType.getId(), equalTo("default"));
        assertThat(groupType.getConceptOrConstraint().size(), equalTo(1));
        RuleType ruleType = groupType.getConceptOrConstraint().get(0);
        assertThat(ruleType, instanceOf(ConstraintType.class));
        assertThat(ruleType.getId(), equalTo("my:Constraint"));
        assertThat(ruleType.getSeverity().getValue(), equalTo("critical"));
        assertThat(ruleType.getStatus(), equalTo(StatusEnumType.FAILURE));
        ResultType result = ruleType.getResult();
        assertThat(result, notNullValue());
        ColumnsHeaderType columnsHeader = result.getColumns();
        List<ColumnHeaderType> columnHeaders = columnsHeader.getColumn();
        verifyColumnHeader(columnHeaders.get(0), "c1", true);
        verifyColumnHeader(columnHeaders.get(1), "c2", false);
    }
}
