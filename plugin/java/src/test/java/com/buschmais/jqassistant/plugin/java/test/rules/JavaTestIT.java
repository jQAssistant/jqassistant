package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class JavaTestIT extends AbstractJavaPluginIT {

    @BeforeEach
    void setUp() {
        query(
            "MERGE (:Artifact)-[:CONTAINS]->(:Java:ByteCode:Type:Class{fqn:'Test'})-[:DECLARES]->(:Java:ByteCode:Member:Method:Test{signature:'void test()'})-[:INVOKES]->(:Java:ByteCode:Member:Method)-[:INVOKES]->(:Java:ByteCode:Member:Method:Assert)<-[:DECLARES]-(:Java:ByteCode:Type{fqn:'Assertions'})");
    }

    @Test
    void javaTestClass() throws RuleException {
        Result<Concept> result = applyConcept("java:TestClass");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(1);
        assertThat(((Column<TypeDescriptor>) result.getRows()
            .get(0)
            .getColumns()
            .get("TestClass")).getValue()).is(typeDescriptor("Test"));
    }

    @Test
    void javaTestMethod() throws RuleException {
        Result<Concept> result = applyConcept("java:TestMethod");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(1);
        Map<String, Column<?>> columns = result.getRows()
            .get(0)
            .getColumns();
        assertThat(((Column<TypeDescriptor>) columns.get("TestClass")).getValue()).is(typeDescriptor("Test"));
        assertThat(((Column<Long>) columns.get("TestMethods")).getValue()).isEqualTo(1l);
    }

    @Test
    void javaAssertMethod() throws RuleException {
        Result<Concept> result = applyConcept("java:AssertMethod");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getRows()).hasSize(1);
        Map<String, Column<?>> columns = result.getRows()
            .get(0)
            .getColumns();
        assertThat(((Column<TypeDescriptor>) columns.get("DeclaringType")).getValue()).is(typeDescriptor("Assertions"));
        assertThat(((Column<Long>) columns.get("AssertMethods")).getValue()).isEqualTo(1l);
    }

    @Test
    void javaTestMethodWithoutAssertion() throws RuleException {
        assertThat(validateConstraint("java:TestMethodWithoutAssertion").getStatus()).isEqualTo(SUCCESS);
        Result<Constraint> result = validateConstraint("java:TestMethodWithoutAssertion", Map.of("javaTestAssertMaxCallDepth", "1"));
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()).hasSize(1);
        Map<String, Column<?>> columns = result.getRows()
            .get(0)
            .getColumns();
        assertThat(((Column<TypeDescriptor>) columns.get("TestClass")).getValue()).is(typeDescriptor("Test"));
        assertThat(((Column<MethodDescriptor>) columns.get("TestMethod")).getValue()).isNotNull();
    }
}
