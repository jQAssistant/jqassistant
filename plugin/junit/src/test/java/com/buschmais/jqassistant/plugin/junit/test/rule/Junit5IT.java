package com.buschmais.jqassistant.plugin.junit.test.rule;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.Assertions4Junit5;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.DisabledTestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.ParamterizedTestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.ParentTestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.RepeatedTestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.StandardTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.TagTestClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.TestTemplateClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.MultipleTagAnnotation;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.MultipleTagAnnotationClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.MultipleTagAnnotationTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.SingleTagAnnotation;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.SingleTagAnnotationClass;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.SingleTagAnnotationTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.report.AbstractJunit5Example;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.report.Junit5Example;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.lang.Boolean.FALSE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class Junit5IT extends AbstractJunitIT {

    @AfterEach
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
    public void nestedTestClassesAreLabledWithNested() throws Exception {
        scanClasses(ParentTestClass.class, ParentTestClass.ChildTestClass.class,
                    ParentTestClass.ChildTestClass.GrandChildTestClass.class);

        assertThat(applyConcept("junit5:NestedTestClass").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("MATCH (c:Class:Junit5:Nested) RETURN c").getColumn("c");
        assertThat(classes,
                   containsInAnyOrder(typeDescriptor(ParentTestClass.ChildTestClass.class),
                                      typeDescriptor(ParentTestClass.ChildTestClass.GrandChildTestClass.class)));
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
                                     methodDescriptor(TagTestClass.C.class, "activeTest")));
        ;
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

        List<TypeDescriptor> classes = query("match (c:Tag:Type:Junit5) return c").getColumn("c");

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

    @Test
    public void constraintTestClassFindsAllClassesWithTests() throws Exception {
        scanClasses(DisabledTestClass.class, ParamterizedTestClass.class, TestTemplateClass.class,
                    RepeatedTestClass.class, TagTestClass.A.class);

        assertThat(applyConcept("junit5:TestClass").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("match (c:Test:Class:Junit5) return c").getColumn("c");

        assertThat(classes, notNullValue());
        assertThat(classes, Matchers.not(Matchers.empty()));
        assertThat(classes, containsInAnyOrder(typeDescriptor(DisabledTestClass.class),
                                               typeDescriptor(RepeatedTestClass.class),
                                               typeDescriptor(TestTemplateClass.class),
                                               typeDescriptor(TagTestClass.A.class),
                                               typeDescriptor(ParamterizedTestClass.class)));
    }


    @Test
    public void surefireTestReportForJunit5IsProcessedCorrectly() throws Exception {
        scanClasses(Junit5Example.class, AbstractJunit5Example.class);
        scanClassPathResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit.test.set.junit5.report.Junit5Example.xml");

        Result<Concept> implementedByResult = applyConcept("junit:TestCaseImplementedByMethod");
        Result<Concept> definedByResult = applyConcept("junit:TestCaseDefinedByClass");

        assertThat(implementedByResult.getStatus(), equalTo(SUCCESS));
        assertThat(implementedByResult.isEmpty(), is(FALSE));
        assertThat(definedByResult.isEmpty(), is(FALSE));
        assertThat(definedByResult.getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        verifyRelationForImplementedBy("inherited", "inherited", "AbstractJunit5Example");
        verifyRelationForDefinedBy("inherited", "Junit5Example");
        verifyRelationForImplementedBy("success", "success", "Junit5Example");
        verifyRelationForDefinedBy("success", "Junit5Example");
        verifyRelationForImplementedBy("error", "error", "Junit5Example");
        verifyRelationForDefinedBy("error", "Junit5Example");
        verifyRelationForImplementedBy("failure", "failure", "Junit5Example");
        verifyRelationForDefinedBy("failure", "Junit5Example");
        verifyRelationForImplementedBy("skipped", "skipped", "Junit5Example");
        verifyRelationForDefinedBy("skipped", "Junit5Example");

        /* todo: Add later verifications for testcases of the methods repeatedTest()
         * and parameterizedTest()
         *
         * At the moment of writing this JUnit 5 provides wrong values for the test report
         * written by Maven Surefire if the test itself is an repeated or parameterized
         * test. In such cases JUnit reports the name of the method and not the FQN of
         * the classes.
         *
         * Complete the test if this will be fixed by JUnit 5.
         *
         * see https://github.com/buschmais/jqa-junit-plugin/issues/2
         * see https://github.com/junit-team/junit5/issues/1182
         *
         * Oliver B. Fischer, 2017-12-17
         */

        store.commitTransaction();
    }

    @Test
    public void methodWithMetaAnnotationWithSingleTagIsProcessedCorrectly() throws Exception {
        scanClasses(SingleTagAnnotation.class, SingleTagAnnotationTest.class);

        Result<Concept> methods = applyConcept("junit5:TaggedMethodWithMetaAnnotation");

        assertThat(methods.getStatus(), equalTo(SUCCESS));
        assertThat(methods.isEmpty(), is(FALSE));

        store.beginTransaction();

        List<TypeDescriptor> tests = query("match (m:Test:Tag:Junit5) return m").getColumn("m");

        assertThat(tests, Matchers.notNullValue());
        assertThat(tests, Matchers.hasSize(1));
        assertThat(tests, hasItem(methodDescriptor(SingleTagAnnotationTest.class, "getInt")));
    }

    @Test
    public void methodWithMetaAnnotationWithMultipleTagsIsProcessedCorrectly() throws Exception {
        scanClasses(MultipleTagAnnotation.class, MultipleTagAnnotationTest.class);

        Result<Concept> methods = applyConcept("junit5:TaggedMethodWithMetaAnnotation");

        assertThat(methods.getStatus(), equalTo(SUCCESS));
        assertThat(methods.isEmpty(), is(FALSE));

        store.beginTransaction();

        List<TypeDescriptor> tests = query("match (m:Test:Tag:Junit5) return m").getColumn("m");

        assertThat(tests, Matchers.notNullValue());
        assertThat(tests, Matchers.hasSize(1));
        assertThat(tests, hasItem(methodDescriptor(MultipleTagAnnotationTest.class, "getLong")));
    }

    @Test
    public void classWithMetaAnnotationWithMultipleTagsIsProcessedCorrectly() throws Exception {
        scanClasses(MultipleTagAnnotation.class, MultipleTagAnnotationClass.class);

        Result<Concept> classes = applyConcept("junit5:TaggedClassWithMetaAnnotation");

        assertThat(classes.getStatus(), equalTo(SUCCESS));
        assertThat(classes.isEmpty(), is(FALSE));

        store.beginTransaction();

        List<TypeDescriptor> tests = query("match (c:Type:Tag:Junit5) return c").getColumn("c");

        assertThat(tests, Matchers.notNullValue());
        assertThat(tests, Matchers.hasSize(1));
        assertThat(tests, hasItem(typeDescriptor(MultipleTagAnnotationClass.class)));
    }

    @Test
    public void classWithMetaAnnotationWithSingleTagIsProcessedCorrectly() throws Exception {
        scanClasses(SingleTagAnnotation.class, SingleTagAnnotationClass.class);

        Result<Concept> classes = applyConcept("junit5:TaggedClassWithMetaAnnotation");

        assertThat(classes.getStatus(), equalTo(SUCCESS));
        assertThat(classes.isEmpty(), is(FALSE));

        store.beginTransaction();

        List<TypeDescriptor> tests = query("match (c:Type:Tag:Junit5) return c").getColumn("c");

        assertThat(tests, Matchers.notNullValue());
        assertThat(tests, Matchers.hasSize(1));
        assertThat(tests, hasItem(typeDescriptor(SingleTagAnnotationClass.class)));
    }

    /**
     * Verifies the concept "junit5:AssertMethod".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void assertMethod() throws Exception {
        scanClasses(Assertions4Junit5.class, Assertions.class);
        assertThat(applyConcept("junit5:AssertMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<MethodDescriptor> methods = query("match (m:Assert:Junit5:Method) return m").getColumn("m");

        assertThat(methods, hasSize(106));
        assertThat(methods, hasItems(methodDescriptor(Assertions.class, "assertTrue", boolean.class),
                                     methodDescriptor(Assertions.class, "assertTrue", boolean.class, String.class)));
        store.commitTransaction();
    }


}
