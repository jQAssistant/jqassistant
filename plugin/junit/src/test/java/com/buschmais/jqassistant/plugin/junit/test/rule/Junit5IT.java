package com.buschmais.jqassistant.plugin.junit.test.rule;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.*;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
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
                   hasItem(typeDescriptor(DisabledTestClass.class)));
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

    @Test
    public void taggedTestsFound() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class,
                    TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TaggedMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:TaggedMethodTags").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<MethodDescriptor> methods = query("MATCH (m:Method:Junit5:Test:Tag) RETURN m").getColumn("m");

        assertThat(methods, notNullValue());
        assertThat(methods, Matchers.not(Matchers.empty()));

        assertThat(methods, hasItems(methodDescriptor(TagTestClass.B.class, "activeTest"),
                                     methodDescriptor(TagTestClass.C.class, "activeTest")));;
    }

    @Test
    public void taggedTestsFoundByTag() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class,
                    TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TaggedMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:TaggedMethodTags").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<MethodDescriptor> methods =
            query("match (m:Test:Method:Junit5) where (\"bm\" in m.tags) return m").getColumn("m");

        assertThat(methods, notNullValue());
        assertThat(methods, Matchers.not(Matchers.empty()));

        assertThat(methods, hasItem(methodDescriptor(TagTestClass.B.class, "activeTest")));
    }

    @Test
    public void taggedTestClassesFound() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class,
                    TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        applyConcept("junit5:RepeatedTestMethod");
        applyConcept("junit5:TestTemplateMethod");
        applyConcept("junit5:ParameterizedTestMethod");

        assertThat(applyConcept("junit5:TaggedClass").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:TaggedClass").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("match (c:Test:Tag:Class:Junit5) return c").getColumn("c");

        assertThat(classes, notNullValue());
        assertThat(classes, Matchers.not(Matchers.empty()));
        assertThat(classes, containsInAnyOrder(typeDescriptor(TagTestClass.A.class),
                                               typeDescriptor(TagTestClass.B.class),
                                               typeDescriptor(TagTestClass.XY.class)));
    }


    @Test
    public void taggedTestClassesFoundByTag() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class,
                    TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        applyConcept("junit5:RepeatedTestMethod");
        applyConcept("junit5:TestTemplateMethod");
        applyConcept("junit5:ParameterizedTestMethod");

        assertThat(applyConcept("junit5:TaggedClassTags").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("match (c:Test:Class:Junit5) where " +
                                             "(\"b\" in c.tags) or " +
                                             "(\"x\" in c.tags) return c").getColumn("c");

        assertThat(classes, notNullValue());
        assertThat(classes, Matchers.not(Matchers.empty()));
        assertThat(classes, containsInAnyOrder(typeDescriptor(TagTestClass.B.class),
                                               typeDescriptor(TagTestClass.XY.class)));
    }
}
