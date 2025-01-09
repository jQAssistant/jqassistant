package com.buschmais.jqassistant.plugin.junit.test.rule;

import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class RuleSetIT extends AbstractJavaPluginIT {

    @Test
    void providedConceptAssertMethod() throws Exception {

        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("java:AssertMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();
        assertThat(conceptResult.getRows().size()).isEqualTo(2);

        final List<TypeDescriptor> declaringTypes = conceptResult.getRows().stream()
            .map(Row::getColumns)
            .map(columns -> columns.get("DeclaringType"))
            .map(Column::getValue)
            .map(TypeDescriptor.class::cast)
            .collect(Collectors.toList());
        assertThat(declaringTypes).haveExactly(1, typeDescriptor(Assertions.class));
        assertThat(declaringTypes).haveExactly(1, typeDescriptor(Mockito.class));

        final List<Long> assertMethods = conceptResult.getRows().stream()
            .map(Row::getColumns)
            .map(columns -> columns.get("AssertMethods"))
            .map(Column::getValue)
            .map(Long.class::cast)
            .collect(Collectors.toList());
        assertThat(assertMethods).contains(1L, 1L);

        final TestResult methodQueryResultForAssertJ = query(
            "MATCH (testMethod:Method)-[:INVOKES]->(assertMethod:Method) "
                + "WHERE assertMethod:AssertJ:Assert "
                + "RETURN testMethod, assertMethod");
        assertThat(methodQueryResultForAssertJ.getRows().size()).isEqualTo(1);
        assertThat(methodQueryResultForAssertJ.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "assertjAssertExampleMethod"));
        assertThat(methodQueryResultForAssertJ.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(Assertions.class, "assertThat", boolean.class));

        final TestResult methodQueryResultForMockito = query(
            "MATCH (testMethod:Method)-[:INVOKES]->(assertMethod:Method) "
                + "WHERE assertMethod:Mockito:Assert "
                + "RETURN testMethod, assertMethod");
        assertThat(methodQueryResultForMockito.getRows().size()).isEqualTo(1);
        assertThat(methodQueryResultForMockito.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "mockitoVerifyExampleMethod"));
        assertThat(methodQueryResultForMockito.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(Mockito.class, "verify", Object.class));

        store.commitTransaction();
    }

}
