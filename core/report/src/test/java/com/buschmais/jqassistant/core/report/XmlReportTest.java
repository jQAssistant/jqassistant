package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportReader;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.rule.api.model.*;

import org.apache.commons.io.FileUtils;
import org.jqassistant.schema.report.v2.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.XmlReportTestHelper.REPORT_DIRECTORY;
import static com.buschmais.jqassistant.core.report.XmlReportTestHelper.ROW_COUNT_VERIFICATION;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlReportTest {

    private static final ReportReader REPORT_READER = new ReportReader();

    private final XmlReportTestHelper xmlReportTestHelper = new XmlReportTestHelper();

    @BeforeEach
    void setUp() {
        FileUtils.deleteQuietly(REPORT_DIRECTORY);
        assertThat(REPORT_DIRECTORY.mkdirs()).isTrue();
    }

    @Test
    void writeAndReadReport() throws ReportException, MalformedURLException {
        File xmlReport = xmlReportTestHelper.createXmlReport(emptyMap());

        JqassistantReport report = readReport(xmlReport);

        assertThat(report).isNotNull();
        verifyContext(report.getContext());
        assertThat(report.getGroupOrConceptOrConstraint()).hasSize(1);
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(groupType.getDate()).isNotNull();
        assertThat(groupType.getId()).isEqualTo("default");
        assertThat(groupType.getGroupOrConceptOrConstraint()).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint()
            .get(0);
        VerificationResultType verificationResult = ruleType.getVerificationResult();
        assertThat(verificationResult.isSuccess()).isTrue();
        assertThat(verificationResult.getRowCount()).isEqualTo(1);
        assertThat(ruleType.getStatus()).isEqualTo(StatusEnumType.SUCCESS);
        assertThat(ruleType).isInstanceOf(ConceptType.class);
        assertThat(ruleType.getId()).isEqualTo("my:concept");
        assertThat(ruleType.getDescription()).isEqualTo("My concept description");
        // Result
        assertThat(ruleType.getResult()).isNotNull();
        ResultType result = ruleType.getResult();
        assertThat(result.getColumns()
            .getCount()).isEqualTo(2);
        assertThat(result.getColumns()
            .getPrimary()).isEqualTo("c2");
        List<String> columnHeaders = result.getColumns()
            .getColumn();
        assertThat(columnHeaders).hasSize(2)
            .containsExactly("c1", "c2");
        assertThat(result.getRows()
            .getCount()).isEqualTo(1);
        List<RowType> rows = result.getRows()
            .getRow();
        assertThat(rows).hasSize(1);
        RowType rowType = rows.get(0);
        assertThat(rowType.getKey()).hasSize(64);
        assertThat(rowType.getColumn()).hasSize(2);
        for (ColumnType column : rowType.getColumn()) {
            assertThat(column.getName()).isIn("c1", "c2");
            if ("c1".equals(column.getName())) {
                assertThat(column.getValue()).isEqualTo("simpleValue");
            } else if ("c2".equals(column.getName())) {
                assertThat(column.getElement()
                    .getLanguage()).isEqualTo("TestLanguage");
                assertThat(column.getElement()
                    .getValue()).isEqualTo("TestElement");
                assertThat(column.getValue()).isEqualTo("descriptorValue");
                SourceLocationType source = column.getSource();
                assertThat(source.getFileName()).isEqualTo("Test.java");
                assertThat(source.getStartLine()).isEqualTo(1);
                assertThat(source.getEndLine()).isEqualTo(2);
            }
        }
        // Reports
        ReportsType reports = ruleType.getReports();
        assertThat(reports).isNotNull();
        List<AbstractReportType> imageOrLink = reports.getImageOrLink();
        assertThat(imageOrLink).hasSize(2);
        Map<String, AbstractReportType> reportsByLabel = imageOrLink.stream()
            .collect(toMap(AbstractReportType::getLabel, r -> r));
        AbstractReportType image = reportsByLabel.get("Image");
        assertThat(image).isInstanceOf(ImageType.class);
        assertThat(image.getValue()).isEqualTo("file:image.png");
        AbstractReportType link = reportsByLabel.get("Link");
        assertThat(link).isInstanceOf(LinkType.class);
        assertThat(link.getValue()).isEqualTo("file:report.csv");

        File htmlReport = new File(xmlReport.getParent(), XmlReportPlugin.REPORT_FILE_HTML);
        assertThat(htmlReport).exists();
    }

    private static void verifyContext(ContextType contextType) {
        assertThat(contextType).isNotNull();
        BuildType buildType = contextType.getBuild();
        assertThat(buildType).isNotNull();
        assertThat(buildType.getName()).isEqualTo("TestBuild");
        assertThat(buildType.getTimestamp()).isNotNull();
        assertThat(buildType.getProperties()).isNotNull();
        List<BuildProperty> buildProperties = buildType.getProperties()
            .getProperty();
        assertThat(buildProperties).hasSize(1);
        BuildProperty buildProperty = buildProperties.get(0);
        assertThat(buildProperty.getKey()).isEqualTo("BRANCH");
        assertThat(buildProperty.getValue()).isEqualTo("develop");
    }

    @Test
    void writeReportWithoutHTML() throws ReportException, MalformedURLException {
        File xmlReport = xmlReportTestHelper.createXmlReport(Map.of(XmlReportPlugin.PROPERTY_XML_REPORT_TRANSFORM_TO_HTML, "false"));

        assertThat(xmlReport).exists();
        File htmlReport = new File(xmlReport.getParent(), XmlReportPlugin.REPORT_FILE_HTML);
        assertThat(htmlReport).doesNotExist();
    }

    @Test
    void testReportWithConstraint() throws ReportException {
        File xmlReport = xmlReportTestHelper.createXmlReportWithConstraints();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report.getGroupOrConceptOrConstraint()).hasSize(1);
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(groupType.getId()).isEqualTo("default");
        assertThat(groupType.getGroupOrConceptOrConstraint()).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(ruleType).isInstanceOf(ConstraintType.class);
        assertThat(ruleType.getId()).isEqualTo("my:Constraint");
        assertThat(ruleType.getSeverity()
            .getValue()).isEqualTo("critical");
        assertThat(ruleType.getStatus()).isEqualTo(StatusEnumType.FAILURE);
        ResultType result = ruleType.getResult();
        assertThat(result).isNotNull();
        ColumnsHeaderType columnsHeader = result.getColumns();
        assertThat(columnsHeader.getCount()).isEqualTo(2);
        assertThat(columnsHeader.getPrimary()).isEqualTo("c1");
        List<String> columnHeaders = columnsHeader.getColumn();
        assertThat(columnHeaders).containsExactly("c1", "c2");
    }

    @Test
    void testReportWithKeyColumns() throws ReportException {
        File xmlReport = xmlReportTestHelper.createXmlReportWithKeyColumns();
        JqassistantReport report = readReport(xmlReport);
        ExecutableRuleType ruleType1 = (ExecutableRuleType) report.getGroupOrConceptOrConstraint()
                .get(0);
        ExecutableRuleType ruleType2 = (ExecutableRuleType) report.getGroupOrConceptOrConstraint()
                .get(1);
        ExecutableRuleType ruleType3 = (ExecutableRuleType) report.getGroupOrConceptOrConstraint()
                .get(2);
       String rowKey1 = ruleType1.getResult().getRows().getRow().get(0).getKey();
       String rowKey2 = ruleType2.getResult().getRows().getRow().get(0).getKey();
       String rowKey3 = ruleType3.getResult().getRows().getRow().get(0).getKey();
       assertThat(rowKey1).isNotEqualTo(rowKey2);
       assertThat(rowKey2).isEqualTo(rowKey3);
    }

    @Test
    void nonExistingKeyColumnThrowsException() {
        assertThatThrownBy(xmlReportTestHelper::createConstraintsWithNonExistingKeyColumn).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportWithRequiredAndProvidedConcepts() throws ReportException {
        XmlReportPlugin xmlReportPlugin = XmlReportTestHelper.getXmlReportPlugin();
        Concept requiredConcept = Concept.builder()
            .id("required-concept")
            .description("required concept")
            .severity(Severity.MINOR)
            .executable(new CypherExecutable(""))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .build())
            .build();
        Concept providingConcept = Concept.builder()
            .id("providing-concept")
            .description("providing concept")
            .severity(Severity.MINOR)
            .executable(new CypherExecutable(""))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .build())
            .build();
        Concept abstractConcept = Concept.builder()
            .id("abstract-concept")
            .description("abstract concept")
            .severity(Severity.MINOR)
            .isAbstract(true)
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .build())
            .build();
        Constraint constraint = Constraint.builder()
            .id("constraint")
            .description("My constraint")
            .severity(Severity.BLOCKER)
            .executable(new CypherExecutable(""))
            .verification(ROW_COUNT_VERIFICATION)
            .report(Report.builder()
                .build())
            .build();

        xmlReportPlugin.begin();

        xmlReportPlugin.beginConcept(requiredConcept, emptyMap(), emptyMap());
        xmlReportPlugin.setResult(getResult(requiredConcept));
        xmlReportPlugin.endConcept();

        xmlReportPlugin.beginConcept(providingConcept, emptyMap(), emptyMap());
        xmlReportPlugin.setResult(getResult(providingConcept));
        xmlReportPlugin.endConcept();

        Map.Entry<Concept, Boolean> requiredConceptRef = new AbstractMap.SimpleEntry<>(requiredConcept, true);
        xmlReportPlugin.beginConcept(abstractConcept, Map.of(requiredConceptRef, Result.Status.SUCCESS), Map.of(providingConcept, Result.Status.SUCCESS));
        xmlReportPlugin.setResult(getResult(abstractConcept));
        xmlReportPlugin.endConcept();

        xmlReportPlugin.beginConstraint(constraint, Map.of(requiredConceptRef, Result.Status.SUCCESS));
        xmlReportPlugin.setResult(getResult(constraint));
        xmlReportPlugin.endConstraint();

        xmlReportPlugin.end();

        JqassistantReport jqassistantReport = readReport(new File("target/test/jqassistant-report.xml"));
        assertThat(jqassistantReport).isNotNull();

        List<ReferencableRuleType> groupOrConceptOrConstraint = jqassistantReport.getGroupOrConceptOrConstraint();
        assertThat(groupOrConceptOrConstraint).hasSize(4);

        ConceptType requiredConceptType = (ConceptType) groupOrConceptOrConstraint.get(0);
        assertThat(requiredConceptType.getId()).isEqualTo("required-concept");
        assertThat(requiredConceptType.isAbstract()).isFalse();
        assertThat(requiredConceptType.getRequiredConcept()).isEmpty();
        assertThat(requiredConceptType.getProvidingConcept()).isEmpty();

        ConceptType providingConceptType = (ConceptType) groupOrConceptOrConstraint.get(1);
        assertThat(providingConceptType.getId()).isEqualTo("providing-concept");
        assertThat(providingConceptType.isAbstract()).isFalse();
        assertThat(providingConceptType.getRequiredConcept()).isEmpty();
        assertThat(providingConceptType.getProvidingConcept()).isEmpty();

        ConceptType abstractConceptType = (ConceptType) groupOrConceptOrConstraint.get(2);
        assertThat(abstractConceptType.getId()).isEqualTo("abstract-concept");
        assertThat(abstractConceptType.isAbstract()).isTrue();
        assertThat(abstractConceptType.getRequiredConcept()).hasSize(1);
        assertThat(abstractConceptType.getRequiredConcept()
            .get(0)
            .getId()).isEqualTo("required-concept");
        assertThat(abstractConceptType.getRequiredConcept()
            .get(0)
            .getStatus()).isEqualTo(StatusEnumType.SUCCESS);
        assertThat(abstractConceptType.getProvidingConcept()).hasSize(1);
        assertThat(abstractConceptType.getProvidingConcept()
            .get(0)
            .getId()).isEqualTo("providing-concept");
        assertThat(abstractConceptType.getProvidingConcept()
            .get(0)
            .getStatus()).isEqualTo(StatusEnumType.SUCCESS);

        ConstraintType constraintType = (ConstraintType) groupOrConceptOrConstraint.get(3);
        assertThat(constraintType.getId()).isEqualTo("constraint");
        assertThat(constraintType.getRequiredConcept()).hasSize(1);
        assertThat(constraintType.getRequiredConcept()
            .get(0)
            .getId()).isEqualTo("required-concept");
        assertThat(constraintType.getRequiredConcept()
            .get(0)
            .getStatus()).isEqualTo(StatusEnumType.SUCCESS);
    }

    @Test
    void reportWithOverrides() throws ReportException {
        XmlReportPlugin xmlReportPlugin = XmlReportTestHelper.getXmlReportPlugin();

        List<String> overriddenA = new LinkedList<>();
        overriddenA.add("overridden-ConceptA");
        overriddenA.add("overridden-ConceptA2");

        List<String> overriddenB = new LinkedList<>();
        overriddenB.add("overridden-ConstraintB");

        List<String> overriddenC = new LinkedList<>();
        overriddenC.add("overridden-GroupC");


        Concept overridingConcept = Concept.builder()
            .id("overriding-Concept")
            .description("This concept overrides another which should be noted additionally in the report.")
            .severity(Severity.MINOR)
            .overrideConcepts(overriddenA)
            .report(Report.builder()
                .build())
            .build();
        Concept nonnecessaryConcept = Concept.builder()
            .id("nonnecessary-Concept")
            .description("This concept does not matter.")
            .severity(Severity.MINOR)
            .report(Report.builder()
                .build())
            .build();

        Constraint overridingconstraint = Constraint.builder()
            .id("overriding-Constraint")
            .severity(Severity.BLOCKER)
            .overrideConstraints(overriddenB)
            .report(Report.builder()
                .build())
            .build();

        Map<String, Severity> concepts = new HashMap<>();
        concepts.put("overriding-Concept", Severity.MINOR);

        Group overridingGroup = Group.builder()
            .id("overriding-Group")
            .description("This group overrides another..")
            .concepts(concepts)
            .overrideGroups(overriddenC)
            .build();
        Group overriddenGroup = Group.builder()
            .id("overridden-Group")
            .description("This group is overridden and should not be seen in the report.")
            .build();

        xmlReportPlugin.begin();

        xmlReportPlugin.beginConcept(overridingConcept, emptyMap(), emptyMap());
        xmlReportPlugin.setResult(getResult(overridingConcept));
        xmlReportPlugin.endConcept();
        xmlReportPlugin.beginConcept(nonnecessaryConcept, emptyMap(), emptyMap());
        xmlReportPlugin.setResult(getResult(nonnecessaryConcept));
        xmlReportPlugin.endConcept();

        xmlReportPlugin.beginConstraint(overridingconstraint);
        xmlReportPlugin.setResult(getResult(overridingconstraint));
        xmlReportPlugin.endConcept();

        xmlReportPlugin.beginGroup(overridingGroup);
        xmlReportPlugin.endGroup();
        xmlReportPlugin.beginGroup(overriddenGroup);
        xmlReportPlugin.endGroup();

        xmlReportPlugin.end();

        JqassistantReport jqassistantReport = readReport(new File("target/test/jqassistant-report.xml"));
        assertThat(jqassistantReport).isNotNull();

        List<ReferencableRuleType> groupOrConceptOrConstraint = jqassistantReport.getGroupOrConceptOrConstraint();
        assertThat(groupOrConceptOrConstraint).hasSize(5);

        List<String> overriddenConcepts = new ArrayList<>();
        overriddenConcepts.add("overridden-ConceptA");
        overriddenConcepts.add("overridden-ConceptA2");

        assertThat(((ConceptType) groupOrConceptOrConstraint.get(0)).getOverridesConcept()
            .stream()
            .map(OverriddenReferenceType::getId)
            .collect(Collectors.toList())
            .containsAll(overriddenConcepts)).isTrue();

        assertThat(((ConceptType) groupOrConceptOrConstraint.get(1)).getOverridesConcept()).isEmpty();
        assertThat(((ConstraintType) groupOrConceptOrConstraint.get(2)).getOverridesConstraint()
            .get(0)
            .getId()).isEqualTo("overridden-ConstraintB");
        assertThat(((GroupType) groupOrConceptOrConstraint.get(3)).getOverridesGroup()
            .get(0)
            .getId()).isEqualTo("overridden-GroupC");
        assertThat(((GroupType) groupOrConceptOrConstraint.get(4)).getOverridesGroup()).isEmpty();

    }

    private static <T extends ExecutableRule<?>> Result<T> getResult(T rule) {
        return Result.<T>builder()
            .rule(rule)
            .verificationResult(VerificationResult.builder()
                .success(false)
                .rowCount(0)
                .build())
            .status(Result.Status.FAILURE)
            .severity(Severity.CRITICAL)
            .columnNames(emptyList())
            .rows(emptyList())
            .build();
    }

    @Test
    void reportEncoding() throws ReportException {
        String specialCharacters = "ÄÖÜß\"'`";
        String value = specialCharacters + "\u0010\u0013";
        File xmlReport = xmlReportTestHelper.createXmlWithExtraCharacters(value);

        JqassistantReport jqassistantReport = readReport(xmlReport);

        List<ReferencableRuleType> groups = jqassistantReport.getGroupOrConceptOrConstraint();
        assertThat(groups).hasSize(1);
        ReferencableRuleType groupType = groups.get(0);
        assertThat(groupType).isInstanceOf(GroupType.class);
        GroupType defaultGroup = (GroupType) groupType;
        List<ReferencableRuleType> concepts = defaultGroup.getGroupOrConceptOrConstraint();
        assertThat(concepts).hasSize(1);
        ReferencableRuleType conceptType = concepts.get(0);
        assertThat(conceptType).isInstanceOf(ConceptType.class);
        ConceptType myConcept = (ConceptType) conceptType;
        assertThat(myConcept.getDescription()).isEqualTo(specialCharacters);
        List<RowType> rows = myConcept.getResult()
            .getRows()
            .getRow();
        ColumnType column = rows.get(0)
            .getColumn()
            .get(0);
        assertThat(column.getValue()).isEqualTo(specialCharacters);
    }

    private JqassistantReport readReport(File xmlReport) {
        return REPORT_READER.read(xmlReport);
    }

    @Test
    void reportWithSuppressedRows() throws ReportException {
        File xmlReport = XmlReportTestHelper.createXmlWithHiddenRows();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report).isNotNull();

        List<ReferencableRuleType> groupOrConceptOrConstraint = report.getGroupOrConceptOrConstraint();
        assertThat(groupOrConceptOrConstraint).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) report.getGroupOrConceptOrConstraint()
                .get(0);

        RowType row0 = ruleType.getResult()
                .getRows()
                .getRow().get(0);
        RowType row1 = ruleType.getResult()
                .getRows()
                .getRow().get(1);
        RowType row2 = ruleType.getResult()
                .getRows()
                .getRow().get(2);

        assertThat(row0
                .getHidden()
                .getSuppression()).isNotNull();
        assertThat(row0
                .getHidden()
                .getBaseline()).isNull();
        assertThat(row0
                .getHidden()
                .getSuppression()
                .getReason()).isEqualTo("Reason for suppressing");
        assertThat(row0
                .getHidden()
                .getSuppression()
                .getUntil()
                .toString()).isEqualTo("2067-03-15");

        assertThat(row1
                .getHidden()
                .getSuppression()).isNull();
        assertThat(row1
                .getHidden()
                .getBaseline()).isNotNull();

        assertThat(row2
                .getHidden()
                .getSuppression()).isNotNull();
        assertThat(row2
                .getHidden()
                .getBaseline()).isNotNull();

        assertThat(ruleType.getResult()
                .getRows()
                .getRow()
                .get(3)
                .getHidden()).isNull();
    }

}
