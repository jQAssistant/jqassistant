package com.buschmais.jqassistant.plugin.junit4.test.rule;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit4.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit4.test.set.Assertions;
import com.buschmais.jqassistant.plugin.junit4.test.set.Example;
import com.buschmais.jqassistant.plugin.junit4.test.set.IgnoredTestClass;
import com.buschmais.jqassistant.plugin.junit4.test.set.TestClass;

/**
 * Tests for Junit4 concepts.
 */
public class Junit4IT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "junit4:TestClassOrMethod".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testClassOrMethod() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(TestClass.class);
        applyConcept("junit4:TestClassOrMethod");
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit4:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
        assertThat(query("MATCH (c:Type:Class:Junit4:Test) RETURN c").getColumn("c"), hasItem(typeDescriptor(TestClass.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:IgnoreTestClassOrMethod".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void ignoreTestClassOrMethod() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(IgnoredTestClass.class);
        applyConcept("junit4:IgnoreTestClassOrMethod");
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit4:Ignore) RETURN m").getColumn("m"), hasItem(methodDescriptor(IgnoredTestClass.class, "ignoredTestMethod")));
        assertThat(query("MATCH (c:Type:Class:Junit4:Ignore) RETURN c").getColumn("c"), hasItem(typeDescriptor(IgnoredTestClass.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:TestCaseImplementedByMethod".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testCaseImplementedByMethod() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(Example.class);
        scanResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml");
        applyConcept("junit4:TestCaseImplementedByMethod");
        store.beginTransaction();
        verifyTestCaseImplementedByMethod("success");
        verifyTestCaseImplementedByMethod("failure");
        verifyTestCaseImplementedByMethod("error");
        verifyTestCaseImplementedByMethod("skipped");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:AssertMethod".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void assertMethod() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(Assertions.class);
        applyConcept("junit4:AssertMethod");
        store.beginTransaction();
        List<Object> methods = query("match (m:Assert:Method) return m").getColumn("m");
        assertThat(
                methods,
                allOf(hasItem(methodDescriptor(Assert.class, "assertTrue", boolean.class)),
                        hasItem(methodDescriptor(Assert.class, "assertTrue", String.class, boolean.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit4:AssertionMustProvideMessage".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void assertionMustProvideMessage() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(Assertions.class);
        validateConstraint("junit4:AssertionMustProvideMessage");
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit4:AssertionMustProvideMessage")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        assertThat((MethodDescriptor) rows.get(0).get("Method"), methodDescriptor(Assertions.class, "assertWithoutMessage"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit4:TestMethodWithoutAssertion".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void testMethodWithoutAssertion() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(Assertions.class);
        validateConstraint("junit4:TestMethodWithoutAssertion");
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit4:TestMethodWithoutAssertion")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        assertThat((MethodDescriptor) rows.get(0).get("Method"), methodDescriptor(Assertions.class, "testWithoutAssertion"));
        store.commitTransaction();
    }

    /**
     * Verifies if a IMPLEMENTED_BY relation exists between a test case and and
     * test method.
     * 
     * @param testcase
     *            The name of the test case.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    private void verifyTestCaseImplementedByMethod(String testcase) throws NoSuchMethodException {
        assertThat(query("MATCH (testcase:TestCase)-[:IMPLEMENTED_BY]->(testmethod:Method) WHERE testcase.name ='" + testcase + "' RETURN testmethod")
                .getColumn("testmethod"), hasItem(methodDescriptor(Example.class, testcase)));
    }
}
