package com.buschmais.jqassistant.plugin.junit.test.rule;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.*;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class Junit5IT extends AbstractJavaPluginIT {

    @After
    public void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    public void activeTestMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "activeTest")));
    }

    @Test
    public void nestedTestsMethodsFound() throws Exception {
        scanClasses(ParentTestClass.class, ParentTestClass.ChildTestClass.class,
                    ParentTestClass.ChildTestClass.GrandChildTestClass.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(ParentTestClass.class, "aTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(ParentTestClass.ChildTestClass.class, "bTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(ParentTestClass.ChildTestClass.GrandChildTestClass.class, "cTest")));
    }

    @Test
    public void parameterizedTestFound() throws Exception {
        scanClasses(ParamterizedTestClass.class);
        assertThat(applyConcept("junit5:ParameterizedTestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();


        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(ParamterizedTestClass.class, "parameterizedTest", String.class)));
        assertThat(query("MATCH (m:Method:Junit5:Parameterized) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(ParamterizedTestClass.class, "parameterizedTest", String.class)));
    }

    @Test
    public void repeatedTestFound() throws Exception {
        scanClasses(RepeatedTestClass.class);
        assertThat(applyConcept("junit5:RepeatedTestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(RepeatedTestClass.class, "repeatedTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test:Repeated) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(RepeatedTestClass.class, "repeatedTest")));
    }

    @Test
    public void disabledTestMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:DisabledTestClassOrMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "disabledTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test:Disabled) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "disabledTest")));
    }

    @Test
    public void disabledTestClass() throws Exception {
        scanClasses(DisabledTestClass.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:DisabledTestClassOrMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (c:Class:Junit5:Disabled) RETURN c").getColumn("c"),
                   hasItem(TypeDescriptorMatcher.typeDescriptor(DisabledTestClass.class)));
    }

    @Test
    public void beforeEachMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:BeforeEach").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:BeforeEach) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "beforeEach")));
    }

    @Test
    public void beforeAllMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:BeforeAll").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:BeforeAll) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "beforeAll")));
    }

    @Test
    public void afterEachMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:AfterEach").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:AfterEach) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "afterEach")));
    }

    @Test
    public void afterAllMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:AfterAll").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:AfterAll) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(StandardTest.class, "afterAll")));
    }


    @Test
    public void testTemplateFound() throws Exception {
        scanClasses(TestTemplateClass.class);
        assertThat(applyConcept("junit5:TestTemplateMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test:Template) RETURN m").getColumn("m"),
                   hasItem(methodDescriptor(TestTemplateClass.class, "templatedMethod", int.class)));
    }
}
