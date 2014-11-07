package com.buschmais.jqassistant.core.report;

import java.io.StringWriter;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;

/**
 * Provides functionality for XML report tests.
 */
public final class XmlReportTestHelper {

    public static final String C1 = "c1";
    public static final String C2 = "c2";

    /**
     * Constructor.
     */
    private XmlReportTestHelper() {
    }

    /**
     * Creates a test report.
     * 
     * @return The test report.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the test fails.
     */
    public static String createXmlReport() throws AnalysisListenerException {
        StringWriter writer = new StringWriter();
        XmlReportWriter xmlReportWriter = new XmlReportWriter(writer);
        xmlReportWriter.begin();
        Concept concept = new Concept("my:concept", "My concept description", Severity.MAJOR, null, "match...", null, Collections.<String, Object> emptyMap(),
                Collections.<String> emptySet());
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("my:concept", Severity.INFO);
        Group group = new Group("default", "My group", concepts, Collections.<String, Severity> emptyMap(), Collections.<String> emptySet());
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConcept(concept);
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put(C1, "simpleValue");
        row.put(C2, new TestDescriptor() {
            @Override
            public String getValue() {
                return "descriptorValue";
            }
        });
        rows.add(row);
        Result<Concept> result = new Result<>(concept, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConcept();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return writer.toString();
    }

    /**
     * Creates a test report with {@link Constraint}.
     * 
     * @return The test report.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the test fails.
     */
    public static String createXmlReportWithConstraints() throws AnalysisListenerException {
        StringWriter writer = new StringWriter();
        XmlReportWriter xmlReportWriter = new XmlReportWriter(writer);
        xmlReportWriter.begin();

        Constraint constraint = new Constraint("my:Constraint", "My constraint description", Severity.BLOCKER, null, "match...", null,
                Collections.<String, Object> emptyMap(), Collections.<String> emptySet());
        Map<String, Severity> constraints = new HashMap<>();
        constraints.put("my:Constraint", Severity.INFO);
        Group group = new Group("default", "My group", Collections.<String, Severity> emptyMap(), constraints, Collections.<String> emptySet());
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConstraint(constraint);
        List<Map<String, Object>> rows = new ArrayList<>();
        Result<Constraint> result = new Result<>(constraint, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConstraint();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return writer.toString();
    }
}
