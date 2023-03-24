package com.buschmais.jqassistant.plugin.junit.test.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.Assertions4Junit4;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.TestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.TestSuite;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.Assertions4Junit5;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

/**
 * Tests for JUnit4 concepts.
 */
public class Junit4IT extends AbstractJunitIT {

    /**
     * Verifies the concept "junit4:TestMethod".
     *
     * @throws IOException
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
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void suiteClass() throws Exception {
        scanClasses(TestSuite.class, TestClass.class);
        assertThat(applyConcept("junit4:SuiteClass").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object>builder().entry("testClass", TestClass.class.getName()).build();
        List<Object> suites =
                query("MATCH (s:Junit4:Suite:Class)-[:CONTAINS_TESTCLASS]->(testClass) WHERE testClass.fqn=$testClass RETURN s", params)
                        .getColumn("s");
        assertThat(suites, hasItem(typeDescriptor(TestSuite.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "junit4:SuiteClass" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void suiteClassUnique() throws Exception {
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("testClass", TestClass.class.getName()).entry("suiteClass", TestSuite.class.getName()).build();
        scanClasses(TestSuite.class, TestClass.class);
        store.beginTransaction();
        // create existing relation with property
        assertThat(query("MATCH (s:Type), (c:Type) WHERE s.fqn=$suiteClass AND c.fqn=$testClass MERGE (s)-[r:CONTAINS_TESTCLASS {prop: 'value'}]->(c) RETURN r", params).getColumn("r").size(), equalTo(1));
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
     * Verifies the concept "junit4:AssertMethod".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void assertMethod() throws Exception {
        scanClasses(Assertions4Junit4.class);
        assertThat(applyConcept("junit4:AssertMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<MethodDescriptor> methods = query("match (m:Assert:Junit4:Method) return m").getColumn("m");
        assertThat(methods, containsInAnyOrder(methodDescriptor(Assert.class, "assertTrue", boolean.class),
                                               methodDescriptor(Assert.class, "assertTrue", String.class, boolean.class)));
        store.commitTransaction();
    }


    /**
     * Verifies the concept "junit4:BeforeMethod".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
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
     * @throws NoSuchMethodException
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
     * @throws NoSuchMethodException
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
     * @throws NoSuchMethodException
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
     */
    @Test
    public void defaultGroup() throws RuleException {
        executeGroup("junit4:Default");
        Map<String, Result<Constraint>> constraintViolations = reportPlugin.getConstraintResults();
        assertThat(constraintViolations.size(), equalTo(3));
        assertThat(constraintViolations.keySet(), hasItems(
            "junit4:AssertionMustProvideMessage",
            "junit4:NonJUnit4TestMethod",
            "junit4:UsageOfJUnit5TestApi"));
    }

    /**
     * Verifies the concept "junit4:InnerTestClass"
     */
    @Test
    public void innerTestClass() throws RuleException {
        scanClasses(TestClass.class);
        Result result = applyConcept("junit4:InnerTestClass");
        assertThat(result.getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (c:Type:Junit4:Test:Inner) RETURN c").getColumn("c"), hasItem(typeDescriptor(TestClass.InnerTestClass.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit4:AssertionMustProvideMessage".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void assertionMustProvideMessage() throws Exception {
        scanClasses(Assertions4Junit4.class);
        assertThat(validateConstraint("junit4:AssertionMustProvideMessage").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit4:AssertionMustProvideMessage")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        assertThat((MethodDescriptor) rows.get(0).get("Method"), methodDescriptor(Assertions4Junit4.class, "assertWithoutMessage"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit4:NonJUnit4TestMethod".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void nonJUnit4TestMethod() throws Exception {
        scanClasses(Assertions4Junit5.class);
        Result<Constraint> result = validateConstraint("junit4:NonJUnit4TestMethod");
        assertThat(result.getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<MethodDescriptor> rows = result.getRows().stream().map(m -> (MethodDescriptor) m.get("TestMethod")).collect(Collectors.toList());
        assertThat(rows.size(), equalTo(9));
        assertThat(rows, containsInAnyOrder(
                is(methodDescriptor(Assertions4Junit5.class, "assertWithoutMessage")),
                is(methodDescriptor(Assertions4Junit5.class, "assertWithMessageSupplier")),
                is(methodDescriptor(Assertions4Junit5.class, "assertWithMessage")),
                is(methodDescriptor(Assertions4Junit5.class, "repeatedTestWithoutAssertion")),
                is(methodDescriptor(Assertions4Junit5.class, "parameterizedTestWithoutAssertion", String.class)),
                is(methodDescriptor(Assertions4Junit5.class, "testWithoutAssertion")),
                is(methodDescriptor(Assertions4Junit5.class, "testWithAssertion")),
                is(methodDescriptor(Assertions4Junit5.class, "testWithNestedAssertion")),
                is(methodDescriptor(Assertions4Junit5.class, "assertWithNonVoidReturn"))));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit4:UsageOfJUnit5TestApi".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void usageOfJUnit5TestApi() throws Exception {
        scanClasses(Assertions4Junit5.class);
        Result<Constraint> result = validateConstraint("junit4:UsageOfJUnit5TestApi");
        assertThat(result.getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<MethodDescriptor> rows = result.getRows().stream().map(m -> (MethodDescriptor) m.get("TestMethod")).collect(Collectors.toList());
        assertThat(rows.size(), equalTo(6));
        assertThat(rows, containsInAnyOrder(
                is(methodDescriptor(Assertions4Junit5.class, "assertWithoutMessage")),
                is(methodDescriptor(Assertions4Junit5.class, "assertWithMessageSupplier")),
                is(methodDescriptor(Assertions4Junit5.class, "assertWithMessage")),
                is(methodDescriptor(Assertions4Junit5.class, "testWithAssertion")),
                is(methodDescriptor(Assertions4Junit5.class, "testWithNestedAssertion")),
                is(methodDescriptor(Assertions4Junit5.class, "assertWithNonVoidReturn"))));
        store.commitTransaction();
    }


}
