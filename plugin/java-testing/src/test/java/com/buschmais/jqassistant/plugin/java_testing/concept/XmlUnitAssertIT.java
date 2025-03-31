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
import org.xmlunit.assertj.XmlAssert;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class XmlUnitAssertIT extends AbstractJavaPluginIT {

    @Test
    void xmlAssertMethod() throws Exception {
        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("xmlunit-assertj:AssertMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();

        assertThat(conceptResult.getRows().size()).isEqualTo(1);
        assertThat(conceptResult.getRows()
            .get(0)
            .getColumns()
            .get("assertMethod")
            .getValue()).asInstanceOf(type(MethodDescriptor.class))
            .is(methodDescriptor(XmlAssert.class, "assertThat", Object.class));

        verifyResultGraph();

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
        assertThat(declaringTypes).haveExactly(1, typeDescriptor(XmlAssert.class));

        verifyResultGraph();

        store.commitTransaction();
    }

    // Expects an open transaction
    private void verifyResultGraph() throws NoSuchMethodException {
        final TestResult methodQueryResult = query(
            "MATCH (testMethod:Method)-[:INVOKES]->(assertMethod:Method) "
                + "WHERE assertMethod:AssertJ:Assert "
                + "RETURN testMethod, assertMethod");
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "xmlAssertExampleMethod"));
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(XmlAssert.class, "assertThat", Object.class));
    }

}
