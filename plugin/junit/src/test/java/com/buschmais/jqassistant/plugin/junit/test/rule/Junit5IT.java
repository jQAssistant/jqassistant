package com.buschmais.jqassistant.plugin.junit.test.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.Assertions4Junit4;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.*;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations.*;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.report.AbstractJunit5Example;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.report.Junit5Example;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.lang.Boolean.FALSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

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

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "activeTest")));
    }

    @Test
    public void nestedTestsMethodsFound() throws Exception {
        scanClasses(ParentTestClass.class, ParentTestClass.ChildTestClass.class, ParentTestClass.ChildTestClass.GrandChildTestClass.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(ParentTestClass.class, "aTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(ParentTestClass.ChildTestClass.class, "bTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
            hasItem(methodDescriptor(ParentTestClass.ChildTestClass.GrandChildTestClass.class, "cTest")));
    }

    @Test
    public void nestedTestClassesAreLabledWithNested() throws Exception {
        scanClasses(ParentTestClass.class, ParentTestClass.ChildTestClass.class, ParentTestClass.ChildTestClass.GrandChildTestClass.class);

        assertThat(applyConcept("junit5:NestedTestClass").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("MATCH (c:Class:Junit5:Nested) RETURN c").getColumn("c");
        assertThat(classes,
            containsInAnyOrder(typeDescriptor(ParentTestClass.ChildTestClass.class), typeDescriptor(ParentTestClass.ChildTestClass.GrandChildTestClass.class)));
    }

    @Test
    public void parameterizedTestFound() throws Exception {
        scanClasses(ParameterizedTestClass.class);
        assertThat(applyConcept("java:TestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"),
            hasItem(methodDescriptor(ParameterizedTestClass.class, "parameterizedTest", String.class)));
        assertThat(query("MATCH (m:Method:Junit5:Parameterized) RETURN m").getColumn("m"),
            hasItem(methodDescriptor(ParameterizedTestClass.class, "parameterizedTest", String.class)));
    }

    @Test
    public void repeatedTestFound() throws Exception {
        scanClasses(RepeatedTestClass.class);
        assertThat(applyConcept("java:TestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(RepeatedTestClass.class, "repeatedTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test:Repeated) RETURN m").getColumn("m"), hasItem(methodDescriptor(RepeatedTestClass.class, "repeatedTest")));
    }

    @Test
    public void disabledTestMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:DisabledTestClassOrMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "disabledTest")));
        assertThat(query("MATCH (m:Method:Junit5:Test:Disabled) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "disabledTest")));
    }

    @Test
    public void disabledTestClass() throws Exception {
        scanClasses(DisabledTestClass.class);
        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:DisabledTestClassOrMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (c:Class:Junit5:Disabled) RETURN c").getColumn("c"), hasItem(typeDescriptor(DisabledTestClass.class)));
    }

    @Test
    public void beforeEachMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:BeforeEach").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:BeforeEach) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "beforeEach")));
    }

    @Test
    public void beforeAllMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:BeforeAll").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:BeforeAll) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "beforeAll")));
    }

    @Test
    public void afterEachMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:AfterEach").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:AfterEach) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "afterEach")));
    }

    @Test
    public void afterAllMethodFound() throws Exception {
        scanClasses(StandardTest.class);
        assertThat(applyConcept("junit5:AfterAll").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:AfterAll) RETURN m").getColumn("m"), hasItem(methodDescriptor(StandardTest.class, "afterAll")));
    }

    @Test
    public void testTemplateFound() throws Exception {
        scanClasses(TestTemplateClass.class);
        assertThat(applyConcept("java:TestMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        assertThat(query("MATCH (m:Method:Junit5:Test:Template) RETURN m").getColumn("m"),
            hasItem(methodDescriptor(TestTemplateClass.class, "templatedMethod", int.class)));
    }

    @Test
    public void taggedTestsFound() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class, TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TaggedMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:TaggedMethodTags").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<MethodDescriptor> methods = query("MATCH (m:Method:Junit5:Test:Tag) RETURN m").getColumn("m");

        assertThat(methods, notNullValue());
        assertThat(methods, Matchers.not(Matchers.empty()));

        assertThat(methods, hasItems(methodDescriptor(TagTestClass.B.class, "activeTest"), methodDescriptor(TagTestClass.C.class, "activeTest")));
    }

    @Test
    public void taggedTestsFoundByTag() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class, TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TaggedMethod").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:TaggedMethodTags").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<MethodDescriptor> methods = query("match (m:Test:Method:Junit5) where (\"bm\" in m.tags) return m").getColumn("m");

        assertThat(methods).isNotNull()
            .isNotEmpty();
        assertThat(methods, hasItem(methodDescriptor(TagTestClass.B.class, "activeTest")));
    }

    @Test
    public void taggedTestClassesFound() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class, TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        applyConcept("junit5:RepeatedTestMethod");
        applyConcept("junit5:TestTemplateMethod");
        applyConcept("junit5:ParameterizedTestMethod");

        assertThat(applyConcept("junit5:TaggedClass").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("junit5:TaggedClass").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("match (c:Tag:Type:Junit5) return c").getColumn("c");

        assertThat(classes).isNotNull()
            .isNotEmpty();
        assertThat(classes,
            containsInAnyOrder(typeDescriptor(TagTestClass.A.class), typeDescriptor(TagTestClass.B.class), typeDescriptor(TagTestClass.XY.class)));
    }

    @Test
    public void taggedTestClassesFoundByTag() throws Exception {
        scanClasses(TagTestClass.class, TagTestClass.A.class, TagTestClass.B.class, TagTestClass.C.class, TagTestClass.XY.class);

        assertThat(applyConcept("junit5:TestMethod").getStatus(), equalTo(SUCCESS));
        applyConcept("junit5:RepeatedTestMethod");
        applyConcept("junit5:TestTemplateMethod");
        applyConcept("junit5:ParameterizedTestMethod");

        assertThat(applyConcept("junit5:TaggedClassTags").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("match (c:Test:Class:Junit5) where " + "(\"b\" in c.tags) or " + "(\"x\" in c.tags) return c").getColumn("c");

        assertThat(classes).isNotNull()
            .isNotEmpty();
        assertThat(classes, containsInAnyOrder(typeDescriptor(TagTestClass.B.class), typeDescriptor(TagTestClass.XY.class)));
    }

    @Test
    public void constraintTestClassFindsAllClassesWithTests() throws Exception {
        scanClasses(DisabledTestClass.class, ParameterizedTestClass.class, TestTemplateClass.class, RepeatedTestClass.class, TagTestClass.A.class);

        assertThat(applyConcept("junit5:TestClass").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();

        List<TypeDescriptor> classes = query("match (c:Test:Class:Junit5) return c").getColumn("c");

        assertThat(classes).isNotNull()
            .isNotEmpty();
        assertThat(classes,
            containsInAnyOrder(typeDescriptor(DisabledTestClass.class), typeDescriptor(RepeatedTestClass.class), typeDescriptor(TestTemplateClass.class),
                typeDescriptor(TagTestClass.A.class), typeDescriptor(ParameterizedTestClass.class)));
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

        assertThat(tests).isNotNull()
            .hasSize(1);
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

        assertThat(tests).isNotNull()
            .hasSize(1);
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

        assertThat(tests).isNotNull()
            .hasSize(1);
        assertThat(tests, hasItem(typeDescriptor(SingleTagAnnotationClass.class)));
    }

    /**
     * Verifies the concept "junit5:AssertMethod".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void assertMethod() throws Exception {
        scanClasses(Assertions4Junit5.class, org.junit.jupiter.api.Assertions.class);
        assertThat(applyConcept("java:AssertMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<MethodDescriptor> methods = query("match (m:Assert:Junit5:Method) return m").getColumn("m");

        // Actual we don't know the exact number of methods here as the number of methods
        // depends on the effective dependencies and might change from version to version
        // Oliver B. Fischer, 2019-02-20
        assertThat(methods).hasSizeGreaterThanOrEqualTo(109);
        assertThat(methods, hasItems(methodDescriptor(Assertions.class, "assertTrue", boolean.class),
            methodDescriptor(Assertions.class, "assertTrue", boolean.class, String.class),
            methodDescriptor(Assertions.class, "assertThrows", Class.class, Executable.class), methodDescriptor(Assertions.class, "fail", String.class),
            methodDescriptor(Assertions.class, "fail")));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit5:AssertionMustProvideMessage".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void assertionMustProvideMessage() throws Exception {
        scanClasses(Assertions4Junit5.class);
        assertThat(validateConstraint("junit5:AssertionMustProvideMessage").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults()
            .values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit5:AssertionMustProvideMessage")));
        List<Row> rows = result.getRows();
        assertThat(rows.size(), equalTo(3));
        assertThat(rows.stream()
            .map(r -> (MethodDescriptor) r.getColumns()
                .get("Method")
                .getValue())
            .collect(Collectors.toList()), containsInAnyOrder(methodDescriptor(Assertions4Junit5.class, "assertWithoutMessage"),
            methodDescriptor(Assertions4Junit5.class, "testWithAssertion"), methodDescriptor(Assertions4Junit5.class, "assertWithNonVoidReturn")));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit5:NonJUnit5TestMethod".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void nonJUnit5TestMethod() throws Exception {
        scanClasses(Assertions4Junit4.class);
        Result<Constraint> result = validateConstraint("junit5:NonJUnit5TestMethod");
        assertThat(result.getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<MethodDescriptor> rows = result.getRows()
            .stream()
            .map(m -> (MethodDescriptor) m.getColumns()
                .get("TestMethod")
                .getValue())
            .collect(Collectors.toList());
        assertThat(rows.size(), equalTo(7));
        assertThat(rows, containsInAnyOrder(is(methodDescriptor(Assertions4Junit4.class, "assertWithoutMessage")),
            is(methodDescriptor(Assertions4Junit4.class, "assertWithMessage")), is(methodDescriptor(Assertions4Junit4.class, "testWithoutAssertion")),
            is(methodDescriptor(Assertions4Junit4.class, "testWithAssertion")), is(methodDescriptor(Assertions4Junit4.class, "testWithNestedAssertion")),
            is(methodDescriptor(Assertions4Junit4.class, "testWithExpectedRuntimeException")),
            is(methodDescriptor(Assertions4Junit4.class, "assertWithMessageButNonVoidReturnType"))));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit5:UsageOfJUnit4TestApi".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void usageOfJUnit4TestApi() throws Exception {
        scanClasses(Assertions4Junit4.class);
        Result<Constraint> result = validateConstraint("junit5:UsageOfJUnit4TestApi");
        assertThat(result.getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<MethodDescriptor> rows = result.getRows()
            .stream()
            .map(m -> (MethodDescriptor) m.getColumns()
                .get("TestMethod")
                .getValue())
            .collect(Collectors.toList());
        assertThat(rows.size(), equalTo(5));
        assertThat(rows, containsInAnyOrder(is(methodDescriptor(Assertions4Junit4.class, "assertWithoutMessage")),
            is(methodDescriptor(Assertions4Junit4.class, "assertWithMessage")), is(methodDescriptor(Assertions4Junit4.class, "testWithAssertion")),
            is(methodDescriptor(Assertions4Junit4.class, "testWithNestedAssertion")),
            is(methodDescriptor(Assertions4Junit4.class, "assertWithMessageButNonVoidReturnType"))));
        store.commitTransaction();
    }

}
