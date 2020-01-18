package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import org.jqassistant.schema.report.v1.*;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlReportTest {

    @Test
    public void writeAndReadReport() throws JAXBException, SAXException, ReportException, IOException {
        File xmlReport = XmlReportTestHelper.createXmlReport();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report).isNotNull();
        assertThat(report.getGroupOrConceptOrConstraint()).hasSize(1);
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint().get(0);
        assertThat(groupType.getDate()).isNotNull();
        assertThat(groupType.getId()).isEqualTo("default");
        assertThat(groupType.getGroupOrConceptOrConstraint()).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint().get(0);
        assertThat(ruleType.getStatus()).isEqualTo(StatusEnumType.SUCCESS);
        assertThat(ruleType).isInstanceOf(ConceptType.class);
        assertThat(ruleType.getId()).isEqualTo("my:concept");
        assertThat(ruleType.getDescription()).isEqualTo("My concept description");
        assertThat(ruleType.getResult()).isNotNull();
        ResultType result = ruleType.getResult();
        assertThat(result.getColumns().getCount()).isEqualTo(2);
        List<ColumnHeaderType> columnHeaders = result.getColumns().getColumn();
        assertThat(columnHeaders).hasSize(2);
        verifyColumnHeader(columnHeaders.get(0), "c1", false);
        verifyColumnHeader(columnHeaders.get(1), "c2", true);
        assertThat(result.getRows().getCount()).isEqualTo(1);
        List<RowType> rows = result.getRows().getRow();
        assertThat(rows).hasSize(1);
        RowType rowType = rows.get(0);
        assertThat(rowType.getColumn().size()).isEqualTo(2);
        for (ColumnType column : rowType.getColumn()) {
            assertThat(column.getName()).isIn("c1", "c2");
            if ("c1".equals(column.getName())) {
                assertThat(column.getValue()).isEqualTo("simpleValue");
            } else if ("c2".equals(column.getName())) {
                assertThat(column.getElement().getLanguage()).isEqualTo("TestLanguage");
                assertThat(column.getElement().getValue()).isEqualTo("TestElement");
                assertThat(column.getValue()).isEqualTo("descriptorValue");
                SourceType source = column.getSource();
                assertThat(source.getName()).isEqualTo("Test.java");
                assertThat(source.getLine()).isEqualTo(1);
            }
        }
    }

    @Test
    public void testReportWithConstraint() throws JAXBException, SAXException, ReportException, IOException {
        File xmlReport = XmlReportTestHelper.createXmlReportWithConstraints();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report.getGroupOrConceptOrConstraint()).hasSize(1);
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint().get(0);
        assertThat(groupType.getId()).isEqualTo("default");
        assertThat(groupType.getGroupOrConceptOrConstraint()).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint().get(0);
        assertThat(ruleType).isInstanceOf(ConstraintType.class);
        assertThat(ruleType.getId()).isEqualTo("my:Constraint");
        assertThat(ruleType.getSeverity().getValue()).isEqualTo("critical");
        assertThat(ruleType.getStatus()).isEqualTo(StatusEnumType.FAILURE);
        ResultType result = ruleType.getResult();
        assertThat(result).isNotNull();
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
        assertThat(groups).hasSize(1);
        ReferencableRuleType groupType = groups.get(0);
        assertThat(groupType).isInstanceOf(GroupType.class);
        GroupType defaultGrroup = (GroupType) groupType;
        List<ReferencableRuleType> concepts = defaultGrroup.getGroupOrConceptOrConstraint();
        assertThat(concepts).hasSize(1);
        ReferencableRuleType conceptType = concepts.get(0);
        assertThat(conceptType).isInstanceOf(ConceptType.class);
        ConceptType meinKonzept = (ConceptType) conceptType;
        assertThat(meinKonzept.getDescription()).isEqualTo(description);
    }

    private JqassistantReport readReport(File xmlReport) throws SAXException, JAXBException, IOException {
        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsdFactory.newSchema(new StreamSource(XmlReportTest.class.getResourceAsStream("/META-INF/xsd/jqassistant-report-v1.8.xsd")));
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        StreamSource streamSource = new StreamSource(new InputStreamReader(new FileInputStream(xmlReport), XmlReportPlugin.ENCODING));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return unmarshaller.unmarshal(streamSource, JqassistantReport.class).getValue();
    }

    private void verifyColumnHeader(ColumnHeaderType columnHeaderC1, String expectedName, boolean isPrimary) {
        assertThat(columnHeaderC1.getValue()).isEqualTo(expectedName);
        assertThat(columnHeaderC1.isPrimary()).isEqualTo(isPrimary);
    }

}
