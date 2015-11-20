package com.buschmais.jqassistant.plugin.testng.test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.testng.test.set.test.TestClass;

/**
 * Tests for TestNG concepts.
 */
public class TestNGIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "testng:TestMethod".
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
        assertThat(applyConcept("testng:TestMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:TestNG:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(TestClass.class, "activeTestMethod")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:TestClass".
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
        assertThat(applyConcept("testng:TestClass").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (c:Type:Class:TestNG:Test) RETURN c").getColumn("c"), hasItem(typeDescriptor(TestClass.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:BeforeMethod".
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
        assertThat(applyConcept("testng:BeforeMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:BeforeMethod:TestNG:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "before")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:AfterMethod".
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
        assertThat(applyConcept("testng:AfterMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:AfterMethod:TestNG:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "after")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:BeforeClassMethod".
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
        assertThat(applyConcept("testng:BeforeClassMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:BeforeClassMethod:TestNG:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "beforeClass")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:AfterClassMethod".
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
        assertThat(applyConcept("testng:AfterClassMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:AfterClassMethod:TestNG:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "afterClass")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:BeforeTestMethod".
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
    public void beforeTestMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("testng:BeforeTestMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:BeforeTestMethod:TestNG:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "beforeTest")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "testng:AfterTestMethod".
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
    public void afterTestMethod() throws Exception {
        scanClasses(TestClass.class);
        assertThat(applyConcept("testng:AfterTestMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> methods = query("match (m:AfterTestMethod:TestNG:Method) return m").getColumn("m");
        assertThat(methods, hasItem(methodDescriptor(TestClass.class, "afterTest")));
        store.commitTransaction();
    }
}
