package com.buschmais.jqassistant.rules.junit4.test;

import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.rules.junit4.test.set.IgnoredTestClass;
import com.buschmais.jqassistant.rules.junit4.test.set.TestClass;
import com.buschmais.jqassistant.scanner.test.matcher.MethodDescriptorMatcher;
import com.buschmais.jqassistant.scanner.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.scanner.test.set.pojo.Pojo;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.scanner.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.scanner.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for Junit4 concepts.
 */
public class JunitIT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "junit4:TestMethod".
     *
     * @throws IOException           If the test fails.
     * @throws ReportWriterException If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void testMethod() throws IOException, ReportWriterException, NoSuchMethodException {
        scanClasses(TestClass.class);
        applyConcept("junit4:TestMethod");
        TestResult testResult = executeQuery("MATCH m:METHOD:JUNIT4:TEST RETURN m");
        Map<String, List<Object>> columns = testResult.getColumns();
        assertThat(columns.get("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
    }

    /**
     * Verifies the concept "junit4:TestClass".
     *
     * @throws IOException           If the test fails.
     * @throws ReportWriterException If the test fails.
     */
    @Test
    public void testClass() throws IOException, ReportWriterException {
        scanClasses(TestClass.class);
        applyConcept("junit4:TestClass");
        TestResult testResult = executeQuery("MATCH c:TYPE:CLASS:JUNIT4:TEST RETURN c");
        Map<String, List<Object>> columns = testResult.getColumns();
        assertThat(columns.get("c"), hasItem(typeDescriptor(TestClass.class)));
    }

    /**
     * Verifies the concept "junit4:IgnoreTestMethod".
     *
     * @throws IOException           If the test fails.
     * @throws ReportWriterException If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void ignoreTestMethod() throws IOException, ReportWriterException, NoSuchMethodException {
        scanClasses(IgnoredTestClass.class);
        applyConcept("junit4:IgnoreTestMethod");
        TestResult testResult = executeQuery("MATCH m:METHOD:JUNIT4:TEST:IGNORE RETURN m");
        Map<String, List<Object>> columns = testResult.getColumns();
        assertThat(columns.get("m"), hasItem(methodDescriptor(IgnoredTestClass.class, "ignoredTestMethod")));
    }

    /**
     * Verifies the concept "junit4:IgnoreTestClass".
     *
     * @throws IOException           If the test fails.
     * @throws ReportWriterException If the test fails.
     */
    @Test
    public void ignoreTestClass() throws IOException, ReportWriterException {
        scanClasses(IgnoredTestClass.class);
        applyConcept("junit4:IgnoreTestClass");
        TestResult testResult = executeQuery("MATCH c:TYPE:CLASS:JUNIT4:TEST:IGNORE RETURN c");
        Map<String, List<Object>> columns = testResult.getColumns();
        assertThat(columns.get("c"), hasItem(typeDescriptor(IgnoredTestClass.class)));
    }
}
