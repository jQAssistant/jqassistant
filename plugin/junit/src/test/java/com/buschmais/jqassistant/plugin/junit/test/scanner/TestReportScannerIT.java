package com.buschmais.jqassistant.plugin.junit.test.scanner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.api.model.TestCaseDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.model.TestCaseDetailDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.model.TestSuiteDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.report.Example;

public class TestReportScannerIT extends AbstractJavaPluginIT {

    /**
     * Verifies that test reports files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void reportFile() throws IOException {
        String testReportFile = "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml";
        scanClassPathResource(JunitScope.TESTREPORTS, testReportFile);
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object>create().put("fileName", testReportFile).get();
        List<FileDescriptor> fileDescriptors =
                query("MATCH (suite:JUnit:File) where suite.fileName={fileName} RETURN suite", params).getColumn
                        ("suite");
        assertThat(fileDescriptors.size(), equalTo(1));
        FileDescriptor fileDescriptor = fileDescriptors.get(0);
        assertThat(fileDescriptor, instanceOf(TestSuiteDescriptor.class));
        TestSuiteDescriptor testSuiteDescriptor = (TestSuiteDescriptor) fileDescriptor;
        assertThat(testSuiteDescriptor.getFileName(), endsWith("TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml"));
        assertThat(testSuiteDescriptor.getTests(), equalTo(4));
        assertThat(testSuiteDescriptor.getFailures(), equalTo(1));
        assertThat(testSuiteDescriptor.getErrors(), equalTo(1));
        assertThat(testSuiteDescriptor.getSkipped(), equalTo(1));
        assertThat(testSuiteDescriptor.getTime(), equalTo(0.058f));
        assertThat(testSuiteDescriptor.getTestCases().size(), equalTo(5));
        verifyTestCase("success", TestCaseDescriptor.Result.SUCCESS, 0.001f);
        verifyTestCase("inherited", TestCaseDescriptor.Result.SUCCESS, 0.002f);
        TestCaseDescriptor failure = verifyTestCase("failure", TestCaseDescriptor.Result.FAILURE, 0.003f);
        verifyDetailDescriptor(failure.getFailure(), AssertionError.class);
        TestCaseDescriptor error = verifyTestCase("error", TestCaseDescriptor.Result.ERROR, 0.004f);
        verifyDetailDescriptor(error.getError(), RuntimeException.class);
        verifyTestCase("skipped", TestCaseDescriptor.Result.SKIPPED, 0.005f);
        store.commitTransaction();
    }

    private TestCaseDescriptor verifyTestCase(String expectedName, TestCaseDescriptor.Result expectedResult, Float expectedTime) {
        List<TestCaseDescriptor> testCaseDescriptors =
                query("MATCH (case:JUnit:TestCase) WHERE case.name='" + expectedName + "' RETURN case").getColumn
                        ("case");
        assertThat(testCaseDescriptors.size(), equalTo(1));
        TestCaseDescriptor testCaseDescriptor = testCaseDescriptors.get(0);
        assertThat(testCaseDescriptor.getName(), equalTo(expectedName));
        assertThat(testCaseDescriptor.getClassName(), equalTo(Example.class.getName()));
        assertThat(testCaseDescriptor.getTime(), equalTo(expectedTime));
        assertThat(testCaseDescriptor.getResult(), equalTo(expectedResult));
        return testCaseDescriptor;
    }

    private void verifyDetailDescriptor(TestCaseDetailDescriptor detailDescriptor, Class<?> expectedType) {
        assertThat(detailDescriptor, notNullValue());
        assertThat(detailDescriptor.getType(), equalTo(expectedType.getName()));
        assertThat(detailDescriptor.getDetails(), containsString(expectedType.getName()));
    }
}
