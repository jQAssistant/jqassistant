package com.buschmais.jqassistant.plugin.java_testing.concept;

import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class MockitoIT extends AbstractJavaPluginIT {

    @Test
    void mockitoVerifyMethod() throws Exception {
        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("mockito:VerifyMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();

        assertThat(conceptResult.getRows().size()).isEqualTo(1);
        assertThat(conceptResult.getRows()
            .get(0)
            .getColumns()
            .get("assertMethod")
            .getValue()).asInstanceOf(type(MethodDescriptor.class))
            .is(methodDescriptor(Mockito.class, "verify", Object.class));

        final TestResult methodQueryResultForMockito = getMethodQueryResultForMockito();
        assertThat(methodQueryResultForMockito.getRows().size()).isEqualTo(1);
        verifyMockitoVerifyExampleContained(methodQueryResultForMockito);

        store.commitTransaction();
    }

    @Test
    void bddMockitoThenShouldMethod() throws Exception {
        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("mockito:BddThenShouldMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();

        assertThat(conceptResult.getRows().size()).isEqualTo(1);
        assertThat(conceptResult.getRows()
            .get(0)
            .getColumns()
            .get("assertMethod")
            .getValue()).asInstanceOf(type(MethodDescriptor.class))
            .is(methodDescriptor(BDDMockito.Then.class, "shouldHaveNoInteractions"));

        final TestResult methodQueryResultForMockito = getMethodQueryResultForMockito();
        assertThat(methodQueryResultForMockito.getRows().size()).isEqualTo(1);
        verifyBddMockitoThenShouldExampleContained(methodQueryResultForMockito);

        store.commitTransaction();
    }

    @Test
    void providedConceptAssertMethod() throws Exception {
        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("java:AssertMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();

        final List<TypeDescriptor> declaringTypes = conceptResult.getRows().stream()
            .map(Row::getColumns)
            .map(columns -> columns.get("DeclaringType"))
            .map(Column::getValue)
            .map(TypeDescriptor.class::cast)
            .collect(Collectors.toList());
        assertThat(declaringTypes).haveExactly(1, typeDescriptor(Mockito.class));

        final TestResult methodQueryResultForMockito = getMethodQueryResultForMockito();
        assertThat(methodQueryResultForMockito.getRows().size()).isEqualTo(2);
        verifyMockitoVerifyExampleContained(methodQueryResultForMockito);
        verifyBddMockitoThenShouldExampleContained(methodQueryResultForMockito);
        store.commitTransaction();
    }

    private TestResult getMethodQueryResultForMockito() {
        return query(
            "MATCH (testMethod:Method)-[:INVOKES]->(assertMethod:Method) "
                + "WHERE assertMethod:Mockito:Assert "
                + "RETURN testMethod, assertMethod");
    }

    private void verifyMockitoVerifyExampleContained(TestResult methodQueryResult) throws NoSuchMethodException {
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "mockitoVerifyExampleMethod"));
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(Mockito.class, "verify", Object.class));
    }

    private void verifyBddMockitoThenShouldExampleContained(TestResult methodQueryResult) throws NoSuchMethodException {
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "bddMockitoThenShouldExampleMethod"));
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(BDDMockito.Then.class, "shouldHaveNoInteractions"));
    }

}
