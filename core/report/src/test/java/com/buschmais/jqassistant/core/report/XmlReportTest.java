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

import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.report.schema.v1.*;

public class XmlReportTest {

    @Test
    public void writeAndReadReport() throws JAXBException, SAXException, ExecutionListenerException {
        String xmlReport = XmlReportTestHelper.createXmlReport();

        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsdFactory.newSchema(new StreamSource(XmlReportTest.class.getResourceAsStream("/META-INF/xsd/jqassistant-report-1.0.xsd")));
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        StreamSource source = new StreamSource(new StringReader(xmlReport));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        JqassistantReport report = unmarshaller.unmarshal(source, JqassistantReport.class).getValue();
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
        assertThat(result.getColumns().getCount(), equalTo(1));
        assertThat(result.getColumns().getColumn(), hasItems("test"));
        assertThat(result.getRows().getCount(), equalTo(1));
        List<RowType> rows = result.getRows().getRow();
        assertThat(rows.size(), equalTo(1));
        RowType rowType = rows.get(0);
        List<ColumnType> columns = rowType.getColumn();
        assertThat(columns.size(), equalTo(1));
        ColumnType column = columns.get(0);
        assertThat(column.getLanguage(), equalTo("testLanguage"));
        assertThat(column.getElement(), equalTo("testElement"));
        assertThat(column.getValue(), equalTo("testValue"));
    }
}
