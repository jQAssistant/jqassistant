package com.buschmais.jqassistant.plugin.junit.test.rule;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.Assertions4Junit4;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.TestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.TestSuite;

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
        Map<String, Result<Constraint>> constraintViolations = reportWriter.getConstraintResults();
        assertThat(constraintViolations, anEmptyMap());
    }
}
