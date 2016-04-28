package com.buschmais.jqassistant.plugin.junit.test.rule;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.test.set.junit3.Junit3Test;

/**
 * Tests for Junit3 concepts.
 */
public class Junit3IT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "junit3:TestClass".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testClass() throws Exception {
        scanClasses(Junit3Test.class);
        assertThat(applyConcept("junit3:TestClass").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (t:Class:Junit3:Test) RETURN t").getColumn("t"), hasItem(typeDescriptor(Junit3Test.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit3:TestMethod".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testMethod() throws Exception {
        scanClasses(Junit3Test.class);
        assertThat(applyConcept("junit3:TestMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit3:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(Junit3Test.class, "testSomething")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit3:SetUpMethod".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void setUpMethod() throws Exception {
        scanClasses(Junit3Test.class);
        assertThat(applyConcept("junit3:SetUpMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit3:SetUp) RETURN m").getColumn("m"), hasItem(methodDescriptor(Junit3Test.class, "setUp")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "junit3:TearDownMethod".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void tearDownMethod() throws Exception {
        scanClasses(Junit3Test.class);
        assertThat(applyConcept("junit3:TearDownMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (m:Method:Junit3:TearDown) RETURN m").getColumn("m"), hasItem(methodDescriptor(Junit3Test.class, "tearDown")));
        store.commitTransaction();
    }
}
