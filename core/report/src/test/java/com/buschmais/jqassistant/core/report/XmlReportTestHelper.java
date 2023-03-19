package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.store.api.Store;

import static com.buschmais.jqassistant.core.report.api.ReportContext.ReportType.IMAGE;
import static com.buschmais.jqassistant.core.report.api.ReportContext.ReportType.LINK;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;

/**
 * Provides functionality for XML report tests.
 */
public final class XmlReportTestHelper {

    public static final String C1 = "c1";
    public static final String C2 = "c2";
    public static final RowCountVerification ROW_COUNT_VERIFICATION = RowCountVerification.builder()
        .build();

    /**
     * Creates a test report.
     *
     * @return The test report.
     * @throws ReportException
     *     If the test fails.
     */
    public File createXmlReport() throws ReportException, MalformedURLException {
        ReportContext reportContext = getReportContext();
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin(reportContext);
        xmlReportPlugin.begin();
        Concept concept = Concept.builder()
            .id("my:concept")
            .description("My concept description")
            .severity(Severity.MAJOR)
            .executable(new CypherExecutable("match..."))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .primaryColumn("c2")
                .build())
            .build();
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("my:concept", Severity.INFO);
        Group group = Group.builder()
            .id("default")
            .description("My group")
            .concepts(concepts)
            .build();
        xmlReportPlugin.beginGroup(group);
        xmlReportPlugin.beginConcept(concept);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Concept> result = Result.<Concept>builder()
            .rule(concept)
            .status(Result.Status.SUCCESS)
            .severity(Severity.CRITICAL)
            .columnNames(Arrays.asList(C1, C2))
            .rows(rows)
            .build();
        xmlReportPlugin.setResult(result);
        reportContext.addReport("Image", concept, IMAGE, new URL("file:image.png"));
        reportContext.addReport("Link", concept, LINK, new URL("file:report.csv"));
        xmlReportPlugin.endConcept();
        xmlReportPlugin.endGroup();
        xmlReportPlugin.end();
        return xmlReportPlugin.getXmlReportFile();
    }

    public File createXmlWithUmlauts(String description) throws ReportException {
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin();
        xmlReportPlugin.begin();
        Concept concept = Concept.builder()
            .id("mein:Konzept")
            .description(description)
            .severity(Severity.MAJOR)
            .executable(new CypherExecutable("match..."))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .primaryColumn("c2")
                .build())
            .build();
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("mein:Konzept", Severity.INFO);
        Group group = Group.builder()
            .id("default")
            .description("Meine Gruppe")
            .concepts(concepts)
            .build();
        xmlReportPlugin.beginGroup(group);
        xmlReportPlugin.beginConcept(concept);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Concept> result = Result.<Concept>builder()
            .rule(concept)
            .status(Result.Status.SUCCESS)
            .severity(Severity.CRITICAL)
            .columnNames(Arrays.asList(C1, C2))
            .rows(rows)
            .build();
        xmlReportPlugin.setResult(result);
        xmlReportPlugin.endConcept();
        xmlReportPlugin.endGroup();
        xmlReportPlugin.end();
        return xmlReportPlugin.getXmlReportFile();
    }

    /**
     * Creates a test report with {@link Constraint}.
     *
     * @return The test report.
     * @throws ReportException
     *     If the test fails.
     */
    public File createXmlReportWithConstraints() throws ReportException {
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin();
        xmlReportPlugin.begin();
        Constraint constraint = Constraint.builder()
            .id("my:Constraint")
            .description("My constraint description")
            .severity(Severity.BLOCKER)
            .executable(new CypherExecutable("match..."))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .build())
            .build();
        Map<String, Severity> constraints = new HashMap<>();
        constraints.put("my:Constraint", Severity.INFO);
        Group group = Group.builder()
            .id("default")
            .description("My group")
            .constraints(constraints)
            .build();
        xmlReportPlugin.beginGroup(group);
        xmlReportPlugin.beginConstraint(constraint);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow());
        Result<Constraint> result = Result.<Constraint>builder()
            .rule(constraint)
            .status(Result.Status.FAILURE)
            .severity(Severity.CRITICAL)
            .columnNames(Arrays.asList(C1, C2))
            .rows(rows)
            .build();
        xmlReportPlugin.setResult(result);
        xmlReportPlugin.endConstraint();
        xmlReportPlugin.endGroup();
        xmlReportPlugin.end();
        return xmlReportPlugin.getXmlReportFile();
    }

    private XmlReportPlugin getXmlReportPlugin() {
        ReportContext reportContext = getReportContext();
        return getXmlReportPlugin(reportContext);
    }

    private XmlReportPlugin getXmlReportPlugin(ReportContext reportContext) {
        XmlReportPlugin xmlReportWriter = new XmlReportPlugin();
        xmlReportWriter.initialize();
        xmlReportWriter.configure(reportContext, emptyMap());
        return xmlReportWriter;
    }

    private static ReportContext getReportContext() {
        File reportDirectory = new File("target/test");
        reportDirectory.mkdirs();
        ReportContext reportContext = new ReportContextImpl(XmlReportTestHelper.class.getClassLoader(), mock(Store.class), reportDirectory);
        return reportContext;
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
