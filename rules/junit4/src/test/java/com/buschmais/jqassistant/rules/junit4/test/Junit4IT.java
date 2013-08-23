package com.buschmais.jqassistant.rules.junit4.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.rules.junit4.test.set.IgnoredTestClass;
import com.buschmais.jqassistant.rules.junit4.test.set.TestClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for Junit4 concepts.
 */
public class Junit4IT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "junit4:TestMethod".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void testMethod() throws IOException, AnalyzerException, NoSuchMethodException {
        scanClasses(TestClass.class);
        applyConcept("junit4:TestMethod");
        Map<String, List<Object>> columns = executeQuery("MATCH m:METHOD:JUNIT4:TEST RETURN m").getColumns();
        assertThat(columns.get("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
    }

    /**
     * Verifies the concept "junit4:TestClass".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void testClass() throws IOException, AnalyzerException {
        scanClasses(TestClass.class);
        applyConcept("junit4:TestClass");
        Map<String, List<Object>> columns = executeQuery("MATCH c:TYPE:CLASS:JUNIT4:TEST RETURN c").getColumns();
        assertThat(columns.get("c"), hasItem(typeDescriptor(TestClass.class)));
    }

    /**
     * Verifies the concept "junit4:IgnoreTestMethod".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void ignoreTestMethod() throws IOException, AnalyzerException, NoSuchMethodException {
        scanClasses(IgnoredTestClass.class);
        applyConcept("junit4:IgnoreTestMethod");
        Map<String, List<Object>> columns = executeQuery("MATCH m:METHOD:JUNIT4:TEST:IGNORE RETURN m").getColumns();
        assertThat(columns.get("m"), hasItem(methodDescriptor(IgnoredTestClass.class, "ignoredTestMethod")));
    }

    /**
     * Verifies the concept "junit4:IgnoreTestClass".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void ignoreTestClass() throws IOException, AnalyzerException {
        scanClasses(IgnoredTestClass.class);
        applyConcept("junit4:IgnoreTestClass");
        Map<String, List<Object>> columns = executeQuery("MATCH c:TYPE:CLASS:JUNIT4:TEST:IGNORE RETURN c").getColumns();
        assertThat(columns.get("c"), hasItem(typeDescriptor(IgnoredTestClass.class)));
    }
}
