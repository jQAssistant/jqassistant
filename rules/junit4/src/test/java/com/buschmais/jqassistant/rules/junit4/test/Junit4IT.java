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
     * Verifies the concept "junit4:TestClassOrMethod".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException     If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void testClassOrMethod() throws IOException, AnalyzerException, NoSuchMethodException {
        scanClasses(TestClass.class);
        applyConcept("junit4:TestClassOrMethod");
        assertThat(executeQuery("MATCH m:METHOD:JUNIT4:TEST RETURN m").getColumns().get("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
        assertThat(executeQuery("MATCH c:TYPE:CLASS:JUNIT4:TEST RETURN c").getColumns().get("c"), hasItem(typeDescriptor(TestClass.class)));
    }

    /**
     * Verifies the concept "junit4:IgnoreTestClassOrMethod".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException     If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void ignoreTestClassOrMethod() throws IOException, AnalyzerException, NoSuchMethodException {
        scanClasses(IgnoredTestClass.class);
        applyConcept("junit4:IgnoreTestClassOrMethod");
        assertThat(executeQuery("MATCH m:METHOD:JUNIT4:IGNORE RETURN m").getColumns().get("m"), hasItem(methodDescriptor(IgnoredTestClass.class, "ignoredTestMethod")));
        assertThat(executeQuery("MATCH c:TYPE:CLASS:JUNIT4:IGNORE RETURN c").getColumns().get("c"), hasItem(typeDescriptor(IgnoredTestClass.class)));
    }
}
