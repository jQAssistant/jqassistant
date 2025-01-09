package com.buschmais.jqassistant.plugin.junit.test.rule;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class AssertJIT extends AbstractJavaPluginIT {

    @Test
    void assertjAssertMethod() throws Exception {
        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("assertj:AssertMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();

        assertThat(conceptResult.getRows().size()).isEqualTo(1);
        assertThat(conceptResult.getRows()
            .get(0)
            .getColumns()
            .get("assertMethod")
            .getValue()).asInstanceOf(type(MethodDescriptor.class))
            .is(methodDescriptor(Assertions.class, "assertThat", boolean.class));

        final TestResult methodQueryResult = query(
            "MATCH (testMethod:Method)-[:INVOKES]->(assertMethod:Method) "
                + "WHERE assertMethod:AssertJ:Assert "
                + "RETURN testMethod, assertMethod");
        assertThat(methodQueryResult.getRows().size()).isEqualTo(1);
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "assertjAssertExampleMethod"));
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(Assertions.class, "assertThat", boolean.class));

        store.commitTransaction();
    }

}
