package com.buschmais.jqassistant.core.report;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.buschmais.jqassistant.core.report.schema.v1.SourceType;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.report.schema.v1.ComplexColumnType;
import com.buschmais.jqassistant.core.report.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.report.schema.v1.GroupType;
import com.buschmais.jqassistant.core.report.schema.v1.JqassistantReport;
import com.buschmais.jqassistant.core.report.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.report.schema.v1.PrimitiveColumnType;
import com.buschmais.jqassistant.core.report.schema.v1.ResultType;
import com.buschmais.jqassistant.core.report.schema.v1.RowType;
import com.buschmais.jqassistant.core.report.schema.v1.RuleType;

public class XmlReportTest {

    @Test
    public void writeAndReadReport() throws JAXBException, SAXException, ExecutionListenerException {
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
        assertThat(rowType.getPrimitiveOrComplex().size(), equalTo(2));
        for (Object o : rowType.getPrimitiveOrComplex()) {
            if (o instanceof PrimitiveColumnType) {
                PrimitiveColumnType primitiveColumn = (PrimitiveColumnType) o;
                assertThat(primitiveColumn.getName(), equalTo("c1"));
                assertThat(primitiveColumn.getValue(), equalTo("simpleValue"));
            } else if (o instanceof ComplexColumnType) {
                ComplexColumnType complexColumn = (ComplexColumnType) o;
                assertThat(complexColumn.getName(), equalTo("c2"));
                assertThat(complexColumn.getLanguage(), equalTo("TestLanguage"));
                assertThat(complexColumn.getElement(), equalTo("TestElement"));
                assertThat(complexColumn.getValue(), equalTo("descriptorValue"));
                SourceType source = complexColumn.getSource();
                assertThat(source.getName(), equalTo("Test.java"));
                assertThat(source.getLine().size(), equalTo(2));
                assertThat(source.getLine().get(0), equalTo(1));
                assertThat(source.getLine().get(1), equalTo(2));
            } else {
                fail("Unknown column " + o);
            }
        }
    }
}
