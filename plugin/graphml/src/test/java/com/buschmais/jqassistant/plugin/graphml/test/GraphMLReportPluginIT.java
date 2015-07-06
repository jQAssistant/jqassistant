package com.buschmais.jqassistant.plugin.graphml.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;
import com.buschmais.jqassistant.plugin.graphml.test.set.a.A;
import com.buschmais.jqassistant.plugin.graphml.test.set.b.B;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

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
        reportAndVerify("test:DeclaredMembers.graphml", 4);
    }

    @Test
    public void renderGraphMLUsingVirtualRelation() throws Exception {
        reportAndVerify("test:DeclaredMembersWithVirtualRelation.graphml", 4);
    }

    @Test
    public void renderGraphMLUsingWithSubgraph() throws Exception {
        Document doc = scanAndWriteReport("test:DeclaredMembersWithSubgraph.graphml", A.class, B.class);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression classExpression = xpath.compile("/graphml/graph/node[contains(@labels,':Class')]/data[@key='fqn']");
        NodeList classes = (NodeList) classExpression.evaluate(doc, XPathConstants.NODESET);

        String[] classNames = { A.class.getName(), B.class.getName() };
        for (int i = 0; i < classes.getLength(); i++) {
            Node item = classes.item(i);
            String value = item.getTextContent();
            Assert.assertTrue("Assert that " + value + " is in list " + Arrays.toString(classNames), ArrayUtils.contains(classNames, value));
        }

        XPathExpression edgeExpression = xpath.compile("//edge");
        NodeList edges = (NodeList) edgeExpression.evaluate(doc, XPathConstants.NODESET);
        assertThat(edges.getLength(), equalTo(2));

    }

    @Test
    public void renderGraphMLUsingWithVirtualNode() throws Exception {
        Document doc = scanAndWriteReport("test:DeclaredMembersWithVirtualNode.graphml", TestClass.class);

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression classExpression = xpath
                .compile("/graphml/graph/node[contains(@labels,':CyclomaticComplexity:Metrics')]/data[@key='totalCyclomaticComplexity']");
        String complexity = classExpression.evaluate(doc);
        assertThat(complexity, equalTo("3"));

    }

    private void reportAndVerify(String conceptName, int assertedEdges) throws Exception {
        Document doc = scanAndWriteReport(conceptName, TestClass.class);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression classExpression = xpath.compile("/graphml/graph/node[contains(@labels,':Class')]/data[@key='fqn']");
        String fqn = classExpression.evaluate(doc);
        assertThat(fqn, equalTo(TestClass.class.getName()));
        XPathExpression declaresExpression = xpath.compile("//edge");
        NodeList edges = (NodeList) declaresExpression.evaluate(doc, XPathConstants.NODESET);
        assertThat(edges.getLength(), equalTo(assertedEdges));
    }

    private Document scanAndWriteReport(String conceptName, Class<?>... scanClasses) throws IOException, AnalysisException, ParserConfigurationException,
            SAXException, FileNotFoundException {
        List<AnalysisListener> reportWriters = new LinkedList<>();
        reportWriters.addAll(getReportPlugins(getReportProperties()));
        CompositeReportWriter compositeReportWriter = new CompositeReportWriter(reportWriters);
        this.analyzer = new AnalyzerImpl(this.store, compositeReportWriter, new TestConsole());
        scanClasses(scanClasses);
        applyConcept(conceptName);
        File reportFile = new File(REPORT_DIR, conceptName.replace(':', '_'));
        assertThat(reportFile.exists(), equalTo(true));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new FileReader(reportFile)));
        return doc;
    }
}
