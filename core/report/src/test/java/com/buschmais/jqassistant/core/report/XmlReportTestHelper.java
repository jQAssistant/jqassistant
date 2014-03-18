package com.buschmais.jqassistant.core.report;

import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;

import java.io.StringWriter;
import java.util.*;

/**
 * Provides functionality for XML report tests.
 */
public final class XmlReportTestHelper {

    public static final String TEST_COLUMN = "test";

    /**
     * Constructor.
     */
    private XmlReportTestHelper() {
    }

    /**
     * Creates a test report.
     *
     * @return The test report.
     * @throws ExecutionListenerException If the test fails.
     */
    public static String createXmlReport() throws ExecutionListenerException {
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
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put(TEST_COLUMN, new TestDescriptor() {
            @Override
            public String toString() {
                return "testValue";
            }
        });
        rows.add(row);
        Result<Concept> result = new Result<>(concept, Arrays.asList(TEST_COLUMN), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConcept();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return writer.toString();
    }
}
