package com.buschmais.jqassistant.core.report;

import java.io.StringWriter;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;

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
     * @throws ReportException If the test fails.
     */
    public static String createXmlReport() throws ReportException {
        StringWriter writer = new StringWriter();
        XmlReportWriter xmlReportWriter = new XmlReportWriter(writer);
        xmlReportWriter.begin();
        Concept concept = Concept.Builder.newConcept().id("my:concept").description("My concept description").severity(Severity.MAJOR)
                .executable(new CypherExecutable("match...")).verification(new RowCountVerification()).report(Report.Builder.newInstance().primaryColumn("c2").get()).get();
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("my:concept", Severity.INFO);
        Group group = Group.Builder.newGroup().id("default").description("My group").conceptIds(concepts).get();
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConcept(concept);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Concept> result = new Result<>(concept, Result.Status.SUCCESS, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
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
     * @throws ReportException If the test fails.
     */
    public static String createXmlReportWithConstraints() throws ReportException {
        StringWriter writer = new StringWriter();
        XmlReportWriter xmlReportWriter = new XmlReportWriter(writer);
        xmlReportWriter.begin();

        Constraint constraint = Constraint.Builder.newConstraint().id("my:Constraint").description("My constraint description")
                .severity(Severity.BLOCKER).executable(new CypherExecutable("match...")).verification(new RowCountVerification()).report(Report.Builder.newInstance().get()).get();
        Map<String, Severity> constraints = new HashMap<>();
        constraints.put("my:Constraint", Severity.INFO);
        Group group = Group.Builder.newGroup().id("default").description("My group").constraintIds(constraints).get();
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConstraint(constraint);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Constraint> result = new Result<>(constraint, Result.Status.FAILURE, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConstraint();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return writer.toString();
    }

    private static Map<String, Object> createRow() {
        Map<String, Object> row = new HashMap<>();
        row.put(C1, "simpleValue");
        row.put(C2, new TestDescriptorWithLanguageElement() {
            @Override
            public String getValue() {
                return "descriptorValue";
            }
        });
        return row;
    }
}
