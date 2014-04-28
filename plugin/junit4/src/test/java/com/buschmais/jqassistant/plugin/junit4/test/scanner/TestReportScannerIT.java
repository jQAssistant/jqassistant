package com.buschmais.jqassistant.plugin.junit4.test.scanner;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor.TestCaseDescriptor;
import com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor.TestSuiteDescriptor;
import com.buschmais.jqassistant.plugin.junit4.test.set.Example;

public class TestReportScannerIT extends AbstractPluginIT {

    /**
     * Verifies that test reports files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void reportFile() throws IOException {
        scanURLs(TestReportScannerIT.class.getResource("/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml"));
        store.beginTransaction();
        List<TestSuiteDescriptor> testSuiteDescriptors = query("MATCH (suite:TESTSUITE:FILE) RETURN suite").getColumn("suite");
        assertThat(testSuiteDescriptors.size(), equalTo(1));
        TestSuiteDescriptor testSuiteDescriptor = testSuiteDescriptors.get(0);
        assertThat(testSuiteDescriptor.getFileName(), endsWith("TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml"));
        assertThat(testSuiteDescriptor.getTests(), equalTo(4));
        assertThat(testSuiteDescriptor.getFailures(), equalTo(1));
        assertThat(testSuiteDescriptor.getErrors(), equalTo(1));
        assertThat(testSuiteDescriptor.getSkipped(), equalTo(1));
        assertThat(testSuiteDescriptor.getTime(), equalTo(0.058f));
        assertThat(testSuiteDescriptor.getTestCases().size(), equalTo(4));
        verifyTestCase("success", TestCaseDescriptor.Result.SUCCESS, 0.001f);
        verifyTestCase("failure", TestCaseDescriptor.Result.FAILURE, 0.003f);
        verifyTestCase("error", TestCaseDescriptor.Result.ERROR, 0.001f);
        verifyTestCase("skipped", TestCaseDescriptor.Result.SKIPPED, 0.001f);
        store.commitTransaction();
    }

    private void verifyTestCase(String expectedName, TestCaseDescriptor.Result expectedResult, Float expectedTime) {
        List<TestCaseDescriptor> testCaseDescriptors = query("MATCH (case:TESTCASE) WHERE case.NAME='" + expectedName + "' RETURN case").getColumn("case");
        assertThat(testCaseDescriptors.size(), equalTo(1));
        TestCaseDescriptor testCaseDescriptor = testCaseDescriptors.get(0);
        assertThat(testCaseDescriptor.getName(), equalTo(expectedName));
        assertThat(testCaseDescriptor.getClassName(), equalTo(Example.class.getName()));
        assertThat(testCaseDescriptor.getTime(), equalTo(expectedTime));
        assertThat(testCaseDescriptor.getResult(), equalTo(expectedResult));
    }
}
