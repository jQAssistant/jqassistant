package com.buschmais.jqassistant.core.report;

import java.io.*;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.report.schema.v1.*;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class XmlReportTest {

    @Test
    public void writeAndReadReport() throws JAXBException, SAXException, ReportException, IOException {
        File xmlReport = XmlReportTestHelper.createXmlReport();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report, notNullValue());
        assertThat(report.getGroupOrConceptOrConstraint().size(), equalTo(1));
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint().get(0);
        assertThat(groupType.getDate(), notNullValue());
        assertThat(groupType.getId(), equalTo("default"));
        assertThat(groupType.getGroupOrConceptOrConstraint().size(), equalTo(1));
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint().get(0);
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

    @Test
    public void testReportWithConstraint() throws JAXBException, SAXException, ReportException, IOException {
        File xmlReport = XmlReportTestHelper.createXmlReportWithConstraints();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report, notNullValue());
        assertThat(report.getGroupOrConceptOrConstraint().size(), equalTo(1));
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint().get(0);
        assertThat(groupType.getDate(), notNullValue());
        assertThat(groupType.getId(), equalTo("default"));
        assertThat(groupType.getGroupOrConceptOrConstraint().size(), equalTo(1));
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint().get(0);
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

    @Test
    public void reportEncoding() throws ReportException, JAXBException, SAXException, IOException {
        String description = "ÄÖÜß";
        File xmlReport = XmlReportTestHelper.createXmlWithUmlauts(description);
        JqassistantReport jqassistantReport = readReport(xmlReport);
        List<ReferencableRuleType> groups = jqassistantReport.getGroupOrConceptOrConstraint();
        assertThat(groups.size(), equalTo(1));
        ReferencableRuleType groupType = groups.get(0);
        assertThat(groupType, instanceOf(GroupType.class));
        GroupType defaultGrroup = (GroupType) groupType;
        List<ReferencableRuleType> concepts = defaultGrroup.getGroupOrConceptOrConstraint();
        assertThat(concepts.size(), equalTo(1));
        ReferencableRuleType conceptType = concepts.get(0);
        assertThat(conceptType, instanceOf(ConceptType.class));
        ConceptType meinKonzept = (ConceptType) conceptType;
        assertThat(meinKonzept.getDescription(), equalTo(description));
    }

    private JqassistantReport readReport(File xmlReport) throws SAXException, JAXBException, IOException {
        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsdFactory.newSchema(new StreamSource(XmlReportTest.class.getResourceAsStream("/META-INF/xsd/jqassistant-report-1.4.xsd")));
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        StreamSource streamSource = new StreamSource(new InputStreamReader(new FileInputStream(xmlReport), XmlReportPlugin.ENCODING));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return unmarshaller.unmarshal(streamSource, JqassistantReport.class).getValue();
    }

    private void verifyColumnHeader(ColumnHeaderType columnHeaderC1, String expectedName, boolean isPrimary) {
        assertThat(columnHeaderC1.getValue(), equalTo(expectedName));
        assertThat(columnHeaderC1.isPrimary(), equalTo(isPrimary));
    }

}
