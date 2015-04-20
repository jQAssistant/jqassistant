package com.buschmais.jqassistant.plugin.graphml.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;
import com.buschmais.jqassistant.plugin.graphml.report.impl.GraphMLReportPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Verifies functionality of the GraphML report plugin.
 */
public class GraphMLReportPluginIT extends AbstractJavaPluginIT {

    static class TestClass {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void renderGraphML() throws Exception {
        GraphMLReportPlugin reportWriter = new GraphMLReportPlugin();
        reportWriter.initialize();
        Map<String, Object> properties = new HashMap<>();
        properties.put("graphml.report.directory", "target/graphml");
        reportWriter.configure(properties);
        this.analyzer = new AnalyzerImpl(this.store, reportWriter, new TestConsole());
        scanClasses(TestClass.class);
        applyConcept("test:Type.graphml");
    }
}
