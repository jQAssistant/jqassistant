package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.CustomAssert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class JavaTestIT extends AbstractJavaPluginIT {

    @BeforeEach
    void setUp() {
        query(
            "MERGE (:Artifact)-[:PROVIDES]->(:Java:ByteCode:Type:Class{fqn:'Test'})-[:DECLARES]->(:Java:ByteCode:Member:Method:Test{signature:'void test()'})-[:INVOKES]->(:Java:ByteCode:Member:Method)-[:INVOKES]->(:Java:ByteCode:Member:Method:Assert)<-[:DECLARES]-(:Java:ByteCode:Type{fqn:'Assertions'})");
        query(
            "MERGE (testType:Java:ByteCode:Type:Class{fqn:'Test'})-[:DECLARES]->(testMethod:Java:ByteCode:Member:Method:Test{signature:'void annotatedTest()'})-[:ANNOTATED_BY]->(:Java:ByteCode:Annotation:Assert)-[:OF_TYPE]->(:Java:ByteCode:Type {fqn: 'AnnotationType'})"
        );
    }

    @Test
    void javaTestClass() throws RuleException {
        Result<Concept> result = applyConcept("java:TestClass");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(1);
        store.beginTransaction();
        assertThat(((Column<TypeDescriptor>) result.getRows()
            .get(0)
            .getColumns()
            .get("TestClass")).getValue()).is(typeDescriptor("Test"));
        store.commitTransaction();
    }

    @Test
    void javaTestMethod() throws RuleException {
        Result<Concept> result = applyConcept("java:TestMethod");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(1);
        store.beginTransaction();
        Map<String, Column<?>> columns = result.getRows()
            .get(0)
            .getColumns();
        assertThat(((Column<TypeDescriptor>) columns.get("TestClass")).getValue()).is(typeDescriptor("Test"));
        assertThat(((Column<Long>) columns.get("TestMethods")).getValue()).isEqualTo(1l);
        store.commitTransaction();
    }

    @Test
    void javaAssertMethod() throws RuleException {
        scanClasses(CustomAssert.class);
        Result<Concept> result = applyConcept("java:AssertMethod");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(2);

        store.beginTransaction();
        assertThat(result.getRows().get(0).getColumns().get("DeclaringType").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor("Assertions"));
        assertThat(result.getRows().get(0).getColumns().get("AssertMethods").getValue()).isEqualTo(1L);
        assertThat(result.getRows().get(1).getColumns().get("DeclaringType").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(1).getColumns().get("AssertMethods").getValue()).isEqualTo(4L);
        store.commitTransaction();
    }

    @Test
    void javaCustomAssertMethod() throws RuleException, NoSuchMethodException {
        scanClasses(CustomAssert.class);
        final Result<Concept> result = applyConcept("java:CustomAssertMethod");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();

        assertThat(result.getRows()).hasSize(4);
        assertThat(result.getRows().get(0).getColumns().get("type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(0).getColumns().get("assertMethod").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodCallingAnotherCustomAssertMethod"));
        assertThat(result.getRows().get(1).getColumns().get("type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(1).getColumns().get("assertMethod").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodDirectlyCallingJunitAssertion"));
        assertThat(result.getRows().get(2).getColumns().get("type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(2).getColumns().get("assertMethod").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodIndirectlyCallingJunitAssertMethod"));
        assertThat(result.getRows().get(3).getColumns().get("type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(3).getColumns().get("assertMethod").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodNotCallingAssertion"));

        final TestResult queryResult = query(
            "MATCH (method:Java:Method:Custom:Assert) RETURN method");
        assertThat(queryResult.getRows()).hasSize(4);
        final List<MethodDescriptor> methods = queryResult.getRows().stream()
                .map(row -> row.get("method"))
                .map(MethodDescriptor.class::cast)
                .collect(Collectors.toList());
        assertThat(methods).haveExactly(1, methodDescriptor(CustomAssert.class, "customAssertMethodCallingAnotherCustomAssertMethod"));
        assertThat(methods).haveExactly(1, methodDescriptor(CustomAssert.class, "customAssertMethodDirectlyCallingJunitAssertion"));
        assertThat(methods).haveExactly(1, methodDescriptor(CustomAssert.class, "customAssertMethodIndirectlyCallingJunitAssertMethod"));
        assertThat(methods).haveExactly(1, methodDescriptor(CustomAssert.class, "customAssertMethodNotCallingAssertion"));
        store.commitTransaction();
    }

    @Test
    void javaCustomAssertMethodWithoutAssertion() throws RuleException, NoSuchMethodException {
        labelJunitAssertTrue();
        scanClasses(CustomAssert.class);
        final Result<Constraint> result = validateConstraint("java:CustomAssertMethodWithoutAssertion");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        store.beginTransaction();

        assertThat(result.getRows()).hasSize(2);

        assertThat(result.getRows().get(0).getColumns().get("Type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(0).getColumns().get("Method").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodCallingAnotherCustomAssertMethod"));

        assertThat(result.getRows().get(1).getColumns().get("Type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(1).getColumns().get("Method").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodNotCallingAssertion"));

        store.commitTransaction();
    }

    @Test
    void javaCustomAssertMethodWithoutAssertionStrictParam() throws RuleException, NoSuchMethodException {
        labelJunitAssertTrue();
        scanClasses(CustomAssert.class);
        final Result<Constraint> result = validateConstraint("java:CustomAssertMethodWithoutAssertion", Map.of("javaCustomAssertMaxCallDepth", "1"));
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        store.beginTransaction();

        assertThat(result.getRows()).hasSize(3);

        assertThat(result.getRows().get(0).getColumns().get("Type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(0).getColumns().get("Method").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodCallingAnotherCustomAssertMethod"));

        assertThat(result.getRows().get(1).getColumns().get("Type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(1).getColumns().get("Method").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodIndirectlyCallingJunitAssertMethod"));

        assertThat(result.getRows().get(2).getColumns().get("Type").getValue()).asInstanceOf(type(TypeDescriptor.class)).is(typeDescriptor(CustomAssert.class));
        assertThat(result.getRows().get(2).getColumns().get("Method").getValue()).asInstanceOf(type(MethodDescriptor.class)).is(methodDescriptor(CustomAssert.class, "customAssertMethodNotCallingAssertion"));

        store.commitTransaction();
    }

    @Test
    void javaAssertAnnotation() throws RuleException {
        Result<Concept> result = applyConcept("java:AssertAnnotation");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(1);
        store.beginTransaction();
        Map<String, Column<?>> columns = result.getRows()
            .get(0)
            .getColumns();
        assertThat(columns.get("DeclaringType").getLabel()).isEqualTo("Test");
        assertThat(columns.get("AnnotatedTestMethod").getLabel()).isEqualTo("void annotatedTest()");
        assertThat(columns.get("AnnotationType").getLabel()).isEqualTo("AnnotationType");
        store.commitTransaction();
    }

    @Test
    void javaMethodPerformsAssertion() throws RuleException {
        Result<Concept> result = applyConcept("java:MethodPerformsAssertion");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(2);
        Map<String, Column<?>> annotationAssertion = result.getRows().get(0).getColumns();
        Map<String, Column<?>> methodAssertion = result.getRows().get(1).getColumns();
        store.beginTransaction();
        assertThat(annotationAssertion.get("DeclaringType").getLabel()).isEqualTo("Test");
        assertThat(annotationAssertion.get("Method").getLabel()).isEqualTo("void annotatedTest()");
        assertThat(methodAssertion.get("DeclaringType").getLabel()).isEqualTo("Test");
        assertThat(methodAssertion.get("Method").getLabel()).isEqualTo("void test()");
        store.commitTransaction();
    }

    @Test
    void javaTestMethodAssertionWithinCallHierarchy() throws RuleException {
        assertThat(validateConstraint("java:TestMethodWithoutAssertion").getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    void javaTestMethodAssertionOutOfCallHierarchy() throws RuleException {
        Result<Constraint> result = validateConstraint("java:TestMethodWithoutAssertion", Map.of("javaTestAssertMaxCallDepth", "1"));
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()).hasSize(1);
        store.beginTransaction();
        Map<String, Column<?>> columns = result.getRows()
            .get(0)
            .getColumns();
        assertThat(((Column<TypeDescriptor>) columns.get("TestClass")).getValue()).is(typeDescriptor("Test"));
        assertThat(((Column<MethodDescriptor>) columns.get("TestMethod")).getValue()).isNotNull();
        store.commitTransaction();
    }

    @Test
    void javaTestMethodAssertionViaAnnotation() throws RuleException {
        assertThat(validateConstraint("java:TestMethodWithoutAssertion").getStatus()).isEqualTo(SUCCESS);
    }

    /**
     * Labels org.junit.jupiter.api.Assertions#assertTrue with :Junit5:Assert.
     */
    private void labelJunitAssertTrue() {
        scanClasses(org.junit.jupiter.api.Assertions.class);
        query("MATCH (assertType:Java:Type {fqn: 'org.junit.jupiter.api.Assertions'})-[:DECLARES]->(assertMethod:Java:Method {name: 'assertTrue'}) SET assertMethod:Junit5:Assert");
    }

}
