package com.buschmais.jqassistant.core.report;


import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.report.schema.v1.GroupType;
import com.buschmais.jqassistant.core.report.schema.v1.JqassistantReport;
import com.buschmais.jqassistant.core.report.schema.v1.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class XmlReportTest {

    @Test
    public void writeAndReadReport() throws JAXBException, SAXException, ExecutionListenerException {
        StringWriter writer = new StringWriter();
        XmlReportWriter xmlReportWriter = new XmlReportWriter(writer);
        xmlReportWriter.begin();
        Group group = new Group();
        group.setId("default");
        Concept concept = new Concept();
        concept.setId("my:concept");
        concept.setDescription("My concept description");

        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConcept(concept);
        Result<Concept> result = new Result<>(concept, Arrays.asList("column1"), Collections.<Map<String, Object>>emptyList());
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConcept();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();

        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsdFactory.newSchema(new StreamSource(XmlReportTest.class.getResourceAsStream("/META-INF/xsd/jqassistant-report-1.0.xsd")));
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        StreamSource source = new StreamSource(new StringReader(writer.toString()));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        JqassistantReport report = unmarshaller.unmarshal(source, JqassistantReport.class).getValue();
        Assert.assertThat(report, notNullValue());
        Assert.assertThat(report.getGroup().size(), equalTo(1));
        GroupType groupType = report.getGroup().get(0);
        Assert.assertThat(groupType.getDate(), notNullValue());
    }
}
