package com.buschmais.jqassistant.plugin.graphml.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Verifies functionality of the GraphML report plugin.
 */
public class GraphMLReportPluginIT extends AbstractJavaPluginIT {

    static class TestClass {

        private String name;

        TestClass() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Override
    protected Map<String, Object> getReportProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("graphml.report.directory", "target/graphml");
        return properties;
    }

    @Test
    public void renderGraphML() throws Exception {
        List<AnalysisListener> reportWriters = new LinkedList<>();
        reportWriters.addAll(getReportPlugins());
        CompositeReportWriter compositeReportWriter = new CompositeReportWriter(reportWriters);
        this.analyzer = new AnalyzerImpl(this.store, compositeReportWriter, new TestConsole());
        scanClasses(TestClass.class);
        applyConcept("test:Type.graphml");
        File reportFile = new File("target/graphml/test_Type.graphml");
        assertThat(reportFile.exists(), equalTo(true));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new FileReader(reportFile)));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression classExpression = xpath.compile("/graphml/graph/node[contains(@labels,':Class')]/data[@key='fqn']");
        String fqn = classExpression.evaluate(doc);
        assertThat(fqn, equalTo(TestClass.class.getName()));
        XPathExpression declaresExpression = xpath.compile("/graphml/graph/edge");
        NodeList edges = (NodeList) declaresExpression.evaluate(doc, XPathConstants.NODESET);
        assertThat(edges.getLength(), equalTo(4));
    }
}
