package com.buschmais.jqassistant.plugin.junit.test.rule;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.NoGroupException;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.assertion.Assertions;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTestWithMessage;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.TestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.TestSuite;
import com.buschmais.jqassistant.plugin.junit.test.set.report.AbstractExample;
import com.buschmais.jqassistant.plugin.junit.test.set.report.Example;

/**
 * Tests for Junit4 concepts.
 */
public class Junit4IT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "junit4:TestMethod".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:TestMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit4:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:TestClass".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testClass() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:TestClass").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (c:Type:Class:Junit4:Test) RETURN c").getColumn("c"), hasItem(typeDescriptor(TestClass.class)));
        store.commitTransaction();
    }

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
    public void testClassOrMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:TestClassOrMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit4:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
        assertThat(query("MATCH (c:Type:Class:Junit4:Test) RETURN c").getColumn("c"), hasItem(typeDescriptor(TestClass.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:SuiteClass".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void suiteClass() throws Exception {
        scanClasses(TestSuite.class, TestClass.class);
        assertThat(applyConcept("junit4:SuiteClass").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object>create("testClass", TestClass.class.getName()).get();
        List<Object> suites =
                query("MATCH (s:Junit4:Suite:Class)-[:CONTAINS_TESTCLASS]->(testClass) WHERE testClass.fqn={testClass} RETURN s", params)
                        .getColumn("s");
        assertThat(suites, hasItem(typeDescriptor(TestSuite.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "junit4:SuiteClass" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void suiteClassUnique() throws Exception {
        Map<String, Object> params = MapBuilder.<String, Object> create("testClass", TestClass.class.getName()).put("suiteClass", TestSuite.class.getName()).get();
    	scanClasses(TestSuite.class, TestClass.class);
        store.beginTransaction();
        // create existing relation with property
        assertThat(query("MATCH (s:Type), (c:Type) WHERE s.fqn={suiteClass} AND c.fqn={testClass} MERGE (s)-[r:CONTAINS_TESTCLASS {prop: 'value'}]->(c) RETURN r", params).getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("CONTAINS_TESTCLASS", 1);
        store.commitTransaction();
        assertThat(applyConcept("junit4:SuiteClass").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("CONTAINS_TESTCLASS", 1);
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
    public void ignoreTestClassOrMethod() throws Exception {
        scanClasses(IgnoredTest.class);
        assertThat(applyConcept("junit4:IgnoreTestClassOrMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (c:Type:Class:Junit4:Ignore) RETURN c").getColumn("c"), hasItem(typeDescriptor(IgnoredTest.class)));
        assertThat(query("MATCH (m:Method:Junit4:Ignore) RETURN m").getColumn("m"), hasItem(methodDescriptor(IgnoredTest.class, "ignoredTest")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:IgnoreWithoutMessage".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void ignoreWithoutMessage() throws Exception {
        scanClasses(IgnoredTest.class, IgnoredTestWithMessage.class);
        assertThat(validateConstraint("junit4:IgnoreWithoutMessage").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit4:IgnoreWithoutMessage")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(2));
        for (Map<String, Object> row : rows) {
            Object ignoredElement = row.get("IgnoreWithoutMessage");
            if (ignoredElement instanceof TypeDescriptor) {
                assertThat((TypeDescriptor) ignoredElement, typeDescriptor(IgnoredTest.class));
            } else if (ignoredElement instanceof MethodDescriptor) {
                assertThat((MethodDescriptor) ignoredElement, methodDescriptor(IgnoredTest.class, "ignoredTest"));
            } else {
                Assert.fail("Unexpected result");
            }
        }
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
    public void testCaseImplementedByMethod() throws Exception {
        scanClasses(AbstractExample.class, Example.class);
        scanClassPathResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml");
        assertThat(applyConcept("junit4:TestCaseImplementedByMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyTestCaseImplementedByMethod(Example.class, "success");
        verifyTestCaseImplementedByMethod(AbstractExample.class, "inherited");
        verifyTestCaseImplementedByMethod(Example.class, "failure");
        verifyTestCaseImplementedByMethod(Example.class, "error");
        verifyTestCaseImplementedByMethod(Example.class, "skipped");
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "junit4:TestCaseImplementedByMethod" with keeping existing properties.
     * 
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testCaseImplementedByMethodUnique() throws Exception {
        scanClasses(Example.class);
        scanClassPathResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml");
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (t:TestCase {name: 'success'}), (m:Method {name: 'success'}) MERGE (t)-[r:IMPLEMENTED_BY {prop: 'value'}]->(m) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'failure'}), (m:Method {name: 'failure'}) MERGE (t)-[r:IMPLEMENTED_BY]->(m) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'success'}), (c:Type {name: 'Example'}) MERGE (t)-[r:DEFINED_BY {prop: 'value'}]->(c) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'failure'}), (c:Type {name: 'Example'}) MERGE (t)-[r:DEFINED_BY]->(c) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("IMPLEMENTED_BY", 2);
        verifyUniqueRelation("DEFINED_BY", 2);
        store.commitTransaction();
        assertThat(applyConcept("junit4:TestCaseImplementedByMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("IMPLEMENTED_BY", 4);
        verifyUniqueRelation("DEFINED_BY", 5);
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
    public void assertMethod() throws Exception {
        scanClasses(Assertions.class);
        assertThat(applyConcept("junit4:AssertMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:Assert:Junit4:Method) return m").getColumn("m");
        assertThat(methods, allOf(hasItem(methodDescriptor(Assert.class, "assertTrue", boolean.class)), hasItem(methodDescriptor(Assert.class,
                "assertTrue", String.class, boolean.class))));
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
    public void assertionMustProvideMessage() throws Exception {
        scanClasses(Assertions.class);
        assertThat(validateConstraint("junit4:AssertionMustProvideMessage").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
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
    public void testMethodWithoutAssertion() throws Exception {
        scanClasses(Assertions.class);
        assertThat(validateConstraint("junit4:TestMethodWithoutAssertion").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit4:TestMethodWithoutAssertion")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        assertThat((MethodDescriptor) rows.get(0).get("Method"), methodDescriptor(Assertions.class, "testWithoutAssertion"));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:BeforeMethod".
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
    public void beforeMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:BeforeMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:Before:Junit4:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "before")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:AfterMethod".
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
    public void afterMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:AfterMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:After:Junit4:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "after")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:BeforeClassMethod".
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
    public void beforeClassMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:BeforeClassMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:BeforeClass:Junit4:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "beforeClass")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit4:AfterClassMethod".
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
    public void afterClassMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("junit4:AfterClassMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:AfterClass:Junit4:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "afterClass")));
        store.commitTransaction();
    }

    /**
     * Verifies the group "junit4:default".
     * 
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void defaultGroup() throws AnalysisException, NoGroupException {
        executeGroup("junit4:Default");
        Map<String, Result<Constraint>> constraintViolations = reportWriter.getConstraintResults();
        assertThat(constraintViolations.keySet(), hasItems("junit4:AssertionMustProvideMessage", "junit4:TestMethodWithoutAssertion",
                "junit4:IgnoreWithoutMessage"));
    }

    /**
     * Verifies if a IMPLEMENTED_BY relation exists between a test case and and test method.
     * 
     * @param declaringType
     *            The class declaring the test method.
     * @param testcase
     *            The name of the test case.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    private void verifyTestCaseImplementedByMethod(Class<?> declaringType, String testcase) throws NoSuchMethodException {
        assertThat(query("MATCH (testcase:TestCase)-[:DEFINED_BY]->(testclass:Type) WHERE testcase.name ='" + testcase + "' RETURN testclass")
                .getColumn("testclass"), hasItem(typeDescriptor(Example.class)));
        assertThat(query(
                "MATCH (testcase:TestCase)-[:IMPLEMENTED_BY]->(testmethod:Method) WHERE testcase.name ='" + testcase + "' RETURN testmethod")
                .getColumn("testmethod"), hasItem(methodDescriptor(declaringType, testcase)));
    }

    /**
     * Verifies a unique relation with property. An existing transaction is assumed.
     * @param relationName The name of the relation.
     * @param total The total of relations with the given name.
     */
    private void verifyUniqueRelation(String relationName, int total) {
    	assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(1));
    	assertThat(query("MATCH ()-[r:" + relationName + "]->() RETURN r").getColumn("r").size(), equalTo(total));
    }
}
