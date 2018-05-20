package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;
import com.buschmais.jqassistant.plugin.junit.impl.schema.Error;
import com.buschmais.jqassistant.plugin.junit.impl.schema.Failure;
import com.buschmais.jqassistant.plugin.junit.impl.schema.Testcase;
import com.buschmais.jqassistant.plugin.junit.impl.schema.Testsuite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JUnitReportPluginTest extends AbstractReportPluginTest {

    private static final String EXPECTED_CONTENT = "c = foo\n" + "---\n" + "c = bar\n";

    private JAXBUnmarshaller<Testsuite> unmarshaller = new JAXBUnmarshaller(Testsuite.class);

    private Group testGroup = Group.builder().id("test:Group").description("testGroup").build();
    private Concept concept = Concept.builder().id("test:Concept").description("testConcept").severity(Severity.MINOR).build();
    private Concept majorConcept = Concept.builder().id("test:MajorConcept").description("testMajorConcept").severity(Severity.MAJOR).build();
    private Concept blockerConcept = Concept.builder().id("test:BlockerConcept").description("testBlockerConcept").severity(Severity.BLOCKER).build();
    private Concept criticalConcept = Concept.builder().id("test:CriticalConcept").description("testCriticalConcept").severity(Severity.CRITICAL).build();
    private Constraint constraint = Constraint.builder().id("test:Constraint").description("testConstraint").severity(Severity.MINOR).build();
    private Constraint majorConstraint = Constraint.builder().id("test:MajorConstraint").description("testMajorConstraint").severity(Severity.MAJOR).build();
    private Constraint blockerConstraint = Constraint.builder().id("test:BlockerConstraint").description("testBlockerConstraint").severity(Severity.BLOCKER)
            .build();
    private Constraint criticalConstraint = Constraint.builder().id("test:CriticalConstraint").description("testCriticalConstraint").severity(Severity.CRITICAL)
            .build();

    public JUnitReportPluginTest() {
        super(new JUnitReportPlugin());
    }

    @Test
    public void junitReport() throws ReportException, IOException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JUnitReportPlugin.JUNIT_FAILURE_SEVERITY, Severity.BLOCKER.name());
        properties.put(JUnitReportPlugin.JUNIT_ERROR_SEVERITY, Severity.CRITICAL.name());

        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(concept, SUCCESS);
        apply(constraint, SUCCESS);
        plugin.beginGroup(testGroup);
        apply(majorConcept, FAILURE);
        apply(blockerConcept, FAILURE);
        apply(criticalConcept, FAILURE);
        apply(majorConstraint, FAILURE);
        apply(blockerConstraint, FAILURE);
        apply(criticalConstraint, FAILURE);
        plugin.endGroup();
        plugin.end();

        File junitReportDirectory = reportContext.getReportDirectory("junit");
        assertThat(junitReportDirectory.exists(), equalTo(true));

        Testsuite rootTestSuite = getTestsuite(junitReportDirectory, "TEST-jQAssistant.xml");
        verifyTestSuite(rootTestSuite, 2, 0, 0);
        Map<String, Testcase> rootTestCases = getTestCases(rootTestSuite);
        verifyTestCaseSuccess(rootTestCases.get("Concept_test_Concept"), "jQAssistant");
        verifyTestCaseSuccess(rootTestCases.get("Constraint_test_Constraint"), "jQAssistant");

        Testsuite groupTestSuite = getTestsuite(junitReportDirectory, "TEST-jQAssistant-test_Group.xml");
        verifyTestSuite(groupTestSuite, 6, 2, 2);
        Map<String, Testcase> groupTestCases = getTestCases(groupTestSuite);

        verifyTestCaseSuccess(groupTestCases.get("Concept_test_MajorConcept"), "jQAssistant-test_Group");
        verifyTestCaseFailure(groupTestCases.get("Concept_test_BlockerConcept"), "jQAssistant-test_Group", "testBlockerConcept");
        verifyTestCaseError(groupTestCases.get("Concept_test_CriticalConcept"), "jQAssistant-test_Group", "testCriticalConcept");

        verifyTestCaseSuccess(groupTestCases.get("Constraint_test_MajorConstraint"), "jQAssistant-test_Group");
        verifyTestCaseFailure(groupTestCases.get("Constraint_test_BlockerConstraint"), "jQAssistant-test_Group", "testBlockerConstraint");
        verifyTestCaseError(groupTestCases.get("Constraint_test_CriticalConstraint"), "jQAssistant-test_Group", "testCriticalConstraint");
    }

    private Map<String, Testcase> getTestCases(Testsuite rootTestSuite) {
        return rootTestSuite.getTestcase().stream().collect(toMap(testCase -> testCase.getName(), testCase -> testCase));
    }

    private void verifyTestSuite(Testsuite testSuite, int expectedTests, int expectedFailures, int expectedErrors) {
        assertThat(testSuite.getTests(), equalTo(Integer.toString(expectedTests)));
        assertThat(testSuite.getFailures(), equalTo(Integer.toString(expectedFailures)));
        assertThat(testSuite.getErrors(), equalTo(Integer.toString(expectedErrors)));
        assertThat(testSuite.getTestcase().size(), equalTo(expectedTests));
        assertThat(Double.valueOf(testSuite.getTime()), greaterThanOrEqualTo(0.0));
    }

    private Testsuite getTestsuite(File junitReportDirectory, String fileName) throws IOException {
        File report = new File(junitReportDirectory, fileName);
        assertThat(report.exists(), equalTo(true));
        return unmarshaller.unmarshal(new FileInputStream(report));
    }

    private void verifyTestCaseSuccess(Testcase testCase, String expectedClassName) {
        verifyTestCase(testCase, expectedClassName);
        assertThat(testCase.getFailure().size(), equalTo(0));
        assertThat(testCase.getError().size(), equalTo(0));
    }

    private void verifyTestCaseFailure(Testcase testCase, String expectedClassName, String expectedMessage) {
        verifyTestCase(testCase, expectedClassName);
        List<Failure> failures = testCase.getFailure();
        assertThat(failures.size(), equalTo(1));
        assertThat(testCase.getError().size(), equalTo(0));
        Failure failure = failures.get(0);
        assertThat(failure.getMessage(), equalTo(expectedMessage));
        assertThat(failure.getContent(), equalTo(EXPECTED_CONTENT));
    }

    private void verifyTestCaseError(Testcase testCase, String expectedClassName, String expectedMessage) {
        verifyTestCase(testCase, expectedClassName);
        assertThat(testCase.getFailure().size(), equalTo(0));
        assertThat(testCase.getError().size(), equalTo(1));
        Error error = testCase.getError().get(0);
        assertThat(error.getMessage(), equalTo(expectedMessage));
        assertThat(error.getContent(), equalTo(EXPECTED_CONTENT));
    }

    private void verifyTestCase(Testcase testCase, String expectedClassName) {
        assertThat(testCase, notNullValue());
        assertThat(testCase.getClassname(), equalTo(expectedClassName));
        assertThat(Double.valueOf(testCase.getTime()), greaterThanOrEqualTo(0.0));
    }

    @Override
    protected <T extends ExecutableRule<?>> Result<T> getResult(T rule, Result.Status status) {
        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("c", "foo");
        HashMap<String, Object> row2 = new HashMap<>();
        row2.put("c", "bar");
        return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(asList("c")).rows(asList(row1, row2)).build();
    }

}
