package com.buschmais.jqassistant.plugin.graphml.test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Verifies functionality of the GraphML report plugin.
 */
public class GraphMLReportPluginIT extends AbstractJavaPluginIT {

    public static final String REPORT_DIR = "target/graphml";

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

    private Map<String, Object> getReportProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("graphml.report.directory", REPORT_DIR);
        return properties;
    }

    @Test
    public void renderGraphML() throws Exception {
        reportAndVerify("test:DeclaredMembers.graphml");
    }

    @Test
    public void renderGraphMLUsingVirtualRelation() throws Exception {
        reportAndVerify("test:DeclaredMembersWithVirtualRelation.graphml");
    }

    private void reportAndVerify(String conceptName) throws Exception {
        List<AnalysisListener> reportWriters = new LinkedList<>();
        reportWriters.addAll(getReportPlugins(getReportProperties()));
        CompositeReportWriter compositeReportWriter = new CompositeReportWriter(reportWriters);
        this.analyzer = new AnalyzerImpl(this.store, compositeReportWriter, new TestConsole());
        scanClasses(TestClass.class);
        applyConcept(conceptName);
        File reportFile = new File(REPORT_DIR, conceptName.replace(':', '_'));
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
