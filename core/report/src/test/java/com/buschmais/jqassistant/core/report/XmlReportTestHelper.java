package com.buschmais.jqassistant.core.report;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Group group = new Group();
        group.setId("default");
        Concept concept = new Concept();
        concept.setId("my:concept");
        concept.setDescription("My concept description");

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

            @Override
            public <I> I getId() {
                return null;
            }

            @Override
            public <T> T as(Class<T> type) {
                return type.cast(this);
            }

            @Override
            public <D> D getDelegate() {
                return null;
            }
        });
        rows.add(row);
        Result<Concept> result = new Result<>(concept, Arrays.asList(C1, C2), rows);
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
        Group group = new Group();
        group.setId("default");
        Constraint constraint = new Constraint();
        constraint.setId("my:Constraint");
        constraint.setDescription("Constraint to verify severity");
        constraint.setSeverity(Severity.CRITICAL);

        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConstraint(constraint);
        List<Map<String, Object>> rows = new ArrayList<>();
        Result<Constraint> result = new Result<>(constraint, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConstraint();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return writer.toString();
    }
}
