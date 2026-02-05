package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.configuration.Build;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.store.api.Store;

import static com.buschmais.jqassistant.core.report.api.ReportContext.ReportType.IMAGE;
import static com.buschmais.jqassistant.core.report.api.ReportContext.ReportType.LINK;
import static com.buschmais.jqassistant.core.report.api.ReportHelper.toColumn;
import static com.buschmais.jqassistant.core.report.api.ReportHelper.toRow;
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

    public static final Build BUILD = new Build() {
        @Override
        public String name() {
            return "TestBuild";
        }

        @Override
        public ZonedDateTime timestamp() {
            return ZonedDateTime.now();
        }

        @Override
        public Map<String, String> properties() {
            return Map.of("BRANCH", "develop");
        }
    };

    public static final File REPORT_DIRECTORY = new File("target/test");

    /**
     * Creates a test report.
     *
     * @param properties
     *     The report properties.
     * @return The test report.
     * @throws ReportException
     *     If the test fails.
     */
    public File createXmlReport(Map<String, Object> properties) throws ReportException, MalformedURLException {
        ReportContext reportContext = getReportContext();
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin(reportContext, properties);
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
        List<Row> rows = new ArrayList<>();
        rows.add(createRow(concept));
        Result<Concept> result = Result.<Concept>builder()
            .rule(concept)
            .verificationResult(VerificationResult.builder()
                .success(true)
                .rowCount(rows.size())
                .build())
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

    public File createXmlWithExtraCharacters(String value) throws ReportException {
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin();
        xmlReportPlugin.begin();
        Concept concept = Concept.builder()
            .id("my:Concept")
            .description(value)
            .severity(Severity.MAJOR)
            .executable(new CypherExecutable("match..."))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .primaryColumn("c1")
                .build())
            .build();
        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("my:Concept", Severity.INFO);
        Group group = Group.builder()
            .id("default")
            .description("My Group")
            .concepts(concepts)
            .build();
        xmlReportPlugin.beginGroup(group);
        xmlReportPlugin.beginConcept(concept);
        List<Row> rows = new ArrayList<>();
        rows.add(Row.builder()
            .key("0")
            .columns(Map.of("C1", Column.builder()
                .value(value)
                .label(value)
                .build()))
            .build());
        Result<Concept> result = Result.<Concept>builder()
            .rule(concept)
            .verificationResult(VerificationResult.builder()
                .success(true)
                .rowCount(rows.size())
                .build())
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
        List<Row> rows = new ArrayList<>();
        rows.add(createRow(constraint));
        Result<Constraint> result = Result.<Constraint>builder()
            .rule(constraint)
            .verificationResult(VerificationResult.builder()
                .success(false)
                .rowCount(rows.size())
                .build())
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

    public File createXmlReportWithKeyColumns() throws ReportException {
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin();
        xmlReportPlugin.begin();

        Constraint keyColumConstraint = Constraint.builder()
                .id("my:Constraint")
                .report(Report.builder().keyColumns(List.of(C1))
                        .build())
                .build();
        Constraint normalConstraint1 = Constraint.builder()
                .id("my:Constraint")
                .report(Report.builder()
                        .build())
                .build();
        Constraint normalConstraint2 = Constraint.builder()
                .id("my:Constraint")
                .report(Report.builder()
                        .build())
                .build();

        List<Constraint> allConstraints = new ArrayList<>();
        allConstraints.add(keyColumConstraint);
        allConstraints.add(normalConstraint1);
        allConstraints.add(normalConstraint2);

        for(Constraint constraint : allConstraints){
            xmlReportPlugin.beginConstraint(constraint);
            List<Row> rows = new ArrayList<>();
            rows.add(createRow(constraint));
            Result<Constraint> result = Result.<Constraint>builder()
                    .rule(constraint)
                    .verificationResult(VerificationResult.builder()
                            .success(false)
                            .rowCount(rows.size())
                            .build())
                    .status(Result.Status.FAILURE)
                    .severity(Severity.CRITICAL)
                    .columnNames(Arrays.asList(C1, C2))
                    .rows(rows)
                    .build();
            xmlReportPlugin.setResult(result);
            xmlReportPlugin.endConstraint();
        }
        xmlReportPlugin.end();
        return xmlReportPlugin.getXmlReportFile();
    }

    public void createConstraintsWithNonExistingKeyColumn() throws ReportException {
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin();
        xmlReportPlugin.begin();
        Constraint constraint = Constraint.builder()
                .id("my:Constraint")
                .report(Report.builder().keyColumns(List.of("c5"))
                        .build())
                .build();

        xmlReportPlugin.beginConstraint(constraint);
        createRow(constraint);
        xmlReportPlugin.endConstraint();
        xmlReportPlugin.end();

    }

    public static XmlReportPlugin getXmlReportPlugin() {
        ReportContext reportContext = getReportContext();
        return getXmlReportPlugin(reportContext, emptyMap());
    }

    public static XmlReportPlugin getXmlReportPlugin(ReportContext reportContext, Map<String, Object> properties) {
        XmlReportPlugin xmlReportWriter = new XmlReportPlugin();
        xmlReportWriter.initialize();
        xmlReportWriter.configure(reportContext, properties);
        return xmlReportWriter;
    }

    private static ReportContext getReportContext() {
        return new ReportContextImpl(BUILD, XmlReportTestHelper.class.getClassLoader(), mock(Store.class), REPORT_DIRECTORY);
    }

    public static File createXmlWithHiddenRows() throws ReportException {
        XmlReportPlugin xmlReportPlugin = getXmlReportPlugin();
        xmlReportPlugin.begin();

        Constraint constraint = Constraint.builder()
                .id("my:Constraint")
                .description("This constraint contains hidden rows.")
                .severity(Severity.MAJOR)
                .executable(new CypherExecutable("match..."))
                .verification(ROW_COUNT_VERIFICATION)
                .report(Report.builder()
                        .primaryColumn("c1")
                        .build())
                .build();

        xmlReportPlugin.beginConstraint(constraint);
        List<Row> rows = new ArrayList<>();
        Hidden hiddenBySuppression = Hidden.builder()
                .build();
        Hidden hiddenByBaseline = Hidden.builder()
                .build();
        Hidden.Suppression suppression = Hidden.Suppression.builder()
                .build();
        suppression.setSuppressReason("Reason for suppressing");
        suppression.setSuppressUntil(LocalDate.of(2067, 3, 15));
        hiddenBySuppression.setSuppression(Optional.of(suppression));

        Hidden.Baseline baseline = Hidden.Baseline.builder()
                .build();
        hiddenByBaseline.setBaseline(Optional.of(baseline));
        Hidden bothSuppressionTypes = Hidden.builder()
                .build();
        bothSuppressionTypes.setBaseline(Optional.of(baseline));
        bothSuppressionTypes.setSuppression(Optional.of(suppression));

        rows.add(Row.builder()
                .key("0")
                .columns(Map.of("c1", Column.builder()
                        .value("suppression")
                        .label("suppression")
                        .build()))
                .hidden(Optional.of(hiddenBySuppression))
                .build());
        rows.add(Row.builder()
                .key("1")
                .columns(Map.of("c1", Column.builder()
                        .value("baseline")
                        .label("baseline")
                        .build()))
                .hidden(Optional.of(hiddenByBaseline))
                .build());
        rows.add(Row.builder()
                .key("2")
                .columns(Map.of("c1", Column.builder()
                        .value("bothSuppressionTypes")
                        .label("bothSuppressionTypes")
                        .build()))
                .hidden(Optional.of(bothSuppressionTypes))
                .build());
        rows.add(Row.builder()
                .key("3")
                .columns(Map.of("c1", Column.builder()
                        .value("nothing")
                        .label("nothing")
                        .build()))
                .build());

        Result<Constraint> result = Result.<Constraint>builder()
                .rule(constraint)
                .verificationResult(VerificationResult.builder()
                        .success(true)
                        .rowCount(rows.size())
                        .build())
                .status(Result.Status.SUCCESS)
                .severity(Severity.CRITICAL)
                .columnNames(Arrays.asList(C1, C2))
                .rows(rows)
                .build();

        xmlReportPlugin.setResult(result);
        xmlReportPlugin.endConstraint();
        xmlReportPlugin.end();
        return xmlReportPlugin.getXmlReportFile();
    }

    private static Row createRow(ExecutableRule<?> rule) {
        Map<String, Column<?>> columns = new HashMap<>();
        columns.put(C1, toColumn("simpleValue"));
        TestDescriptorWithLanguageElement testDescriptor = new TestDescriptorWithLanguageElement() {
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
        };
        columns.put(C2, toColumn(testDescriptor));
        return toRow(rule, columns);
    }
}
