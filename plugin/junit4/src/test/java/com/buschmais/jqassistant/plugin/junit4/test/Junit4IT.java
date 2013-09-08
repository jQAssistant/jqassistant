package com.buschmais.jqassistant.plugin.junit4.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.junit4.test.set.IgnoredTestClass;
import com.buschmais.jqassistant.plugin.junit4.test.set.TestClass;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for Junit4 concepts.
 */
public class Junit4IT extends AbstractPluginIT {

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
        assertThat(query("MATCH m:METHOD:JUNIT4:TEST RETURN m").getColumn("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
        assertThat(query("MATCH c:TYPE:CLASS:JUNIT4:TEST RETURN c").getColumn("c"), hasItem(typeDescriptor(TestClass.class)));
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
        assertThat(query("MATCH m:METHOD:JUNIT4:IGNORE RETURN m").getColumn("m"), hasItem(methodDescriptor(IgnoredTestClass.class, "ignoredTestMethod")));
        assertThat(query("MATCH c:TYPE:CLASS:JUNIT4:IGNORE RETURN c").getColumn("c"), hasItem(typeDescriptor(IgnoredTestClass.class)));
    }
}
