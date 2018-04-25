package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

/**
 * Provides functionality for XML report tests.
 */
public final class XmlReportTestHelper {

    public static final String C1 = "c1";
    public static final String C2 = "c2";
    public static final RowCountVerification ROW_COUNT_VERIFICATION = RowCountVerification.builder().build();

    /**
     * Constructor.
     */
    private XmlReportTestHelper() {
    }

    /**
     * Creates a test report.
     *
     * @return The test report.
     * @throws ReportException
     *             If the test fails.
     */
    public static File createXmlReport() throws ReportException {
        XmlReportPlugin xmlReportWriter = getXmlReportWriter();
        xmlReportWriter.begin();
        Concept concept = Concept.builder().id("my:concept").description("My concept description").severity(Severity.MAJOR)
                .executable(new CypherExecutable("match...")).verification(ROW_COUNT_VERIFICATION)
                .report(Report.Builder.newInstance().primaryColumn("c2").get()).build();
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("my:concept", Severity.INFO);
        Group group = Group.builder().id("default").description("My group").conceptIds(concepts).build();
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConcept(concept);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Concept> result = new Result<>(concept, Result.Status.SUCCESS, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConcept();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return xmlReportWriter.getXmlReportFile();
    }

    public static File createXmlWithUmlauts(String description) throws ReportException {
        XmlReportPlugin xmlReportWriter = getXmlReportWriter();
        xmlReportWriter.begin();
        Concept concept = Concept.builder().id("mein:Konzept").description(description).severity(Severity.MAJOR).executable(new CypherExecutable("match..."))
                .verification(ROW_COUNT_VERIFICATION).report(Report.Builder.newInstance().primaryColumn("c2").get()).build();
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("mein:Konzept", Severity.INFO);
        Group group = Group.builder().id("default").description("Meine Gruppe").conceptIds(concepts).build();
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConcept(concept);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Concept> result = new Result<>(concept, Result.Status.SUCCESS, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConcept();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return xmlReportWriter.getXmlReportFile();
    }

    /**
     * Creates a test report with {@link Constraint}.
     *
     * @return The test report.
     * @throws ReportException
     *             If the test fails.
     */
    public static File createXmlReportWithConstraints() throws ReportException {
        XmlReportPlugin xmlReportWriter = getXmlReportWriter();
        xmlReportWriter.begin();

        Constraint constraint = Constraint.builder().id("my:Constraint").description("My constraint description").severity(Severity.BLOCKER)
                .executable(new CypherExecutable("match...")).verification(ROW_COUNT_VERIFICATION).report(Report.Builder.newInstance().get()).build();
        Map<String, Severity> constraints = new HashMap<>();
        constraints.put("my:Constraint", Severity.INFO);
        Group group = Group.builder().id("default").description("My group").constraintIds(constraints).build();
        xmlReportWriter.beginGroup(group);
        xmlReportWriter.beginConstraint(constraint);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Constraint> result = new Result<>(constraint, Result.Status.FAILURE, Severity.CRITICAL, Arrays.asList(C1, C2), rows);
        xmlReportWriter.setResult(result);
        xmlReportWriter.endConstraint();
        xmlReportWriter.endGroup();
        xmlReportWriter.end();
        return xmlReportWriter.getXmlReportFile();
    }

    private static XmlReportPlugin getXmlReportWriter() {
        XmlReportPlugin xmlReportWriter = new XmlReportPlugin();
        xmlReportWriter.initialize();
        File reportDirectory = new File("target/test");
        reportDirectory.mkdirs();
        xmlReportWriter.configure(new ReportContextImpl(reportDirectory), Collections.EMPTY_MAP);
        return xmlReportWriter;
    }

    private static Map<String, Object> createRow() {
        Map<String, Object> row = new HashMap<>();
        row.put(C1, "simpleValue");
        row.put(C2, new TestDescriptorWithLanguageElement() {
            @Override
            public <I> I getId() {
                return null;
            }

            @Override
            public <T> T as(Class<T> type) {
                return null;
            }

            @Override
            public <D> D getDelegate() {
                return null;
            }

            @Override
            public String getValue() {
                return "descriptorValue";
            }
        });
        return row;
    }
}
