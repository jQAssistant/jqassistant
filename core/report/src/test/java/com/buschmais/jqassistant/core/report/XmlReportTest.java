package com.buschmais.jqassistant.core.report;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
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

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.buschmais.jqassistant.core.report.schema.v1.ColumnType;
import com.buschmais.jqassistant.core.report.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.report.schema.v1.GroupType;
import com.buschmais.jqassistant.core.report.schema.v1.JqassistantReport;
import com.buschmais.jqassistant.core.report.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.report.schema.v1.ResultType;
import com.buschmais.jqassistant.core.report.schema.v1.RowType;
import com.buschmais.jqassistant.core.report.schema.v1.RuleType;
import com.buschmais.jqassistant.core.report.schema.v1.SourceType;

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
        assertThat(ruleType, instanceOf(ConceptType.class));
        assertThat(ruleType.getId(), equalTo("my:concept"));
        assertThat(ruleType.getDescription(), equalTo("My concept description"));
        assertThat(ruleType.getResult(), notNullValue());
        ResultType result = ruleType.getResult();
        assertThat(result.getColumns().getCount(), equalTo(2));
        assertThat(result.getColumns().getColumn(), hasItems("c1", "c2"));
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
}
